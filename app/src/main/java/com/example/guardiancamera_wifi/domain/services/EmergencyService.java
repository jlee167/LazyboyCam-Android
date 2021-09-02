package com.example.guardiancamera_wifi.domain.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import androidx.annotation.Nullable;

import com.example.guardiancamera_wifi.data.configs.Addresses;
import com.example.guardiancamera_wifi.domain.models.ClientStreamData;
import com.example.guardiancamera_wifi.domain.models.EmergencyMessages;
import com.example.guardiancamera_wifi.domain.models.VideoConfig;
import com.example.guardiancamera_wifi.data.configs.WifiCameraProtocol;
import com.example.guardiancamera_wifi.MyApplication;
import com.example.guardiancamera_wifi.data.api.http.base.HttpConnection;
import com.example.guardiancamera_wifi.data.configs.StreamingURI;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


/**
 * Background service that manages stream and emergency protocol.
 *
 * 1. Connect to streaming server and acquire a stream instance (video, audio. geo).
 * 2. Listen to TCP socket for connection from Wifi camera.
 * 3. Pass camera-side stream (video, audio) adresses camera.
 * 4. On emergency request from camera, relay emergency message to streaming server.
 * 5. Stop emergency when user press button.
 *
 * - Broadcast each situation to parent activity for
 *   UI updates and managing GeoLocation stream service..
 *
 */
public class EmergencyService extends Service {

    private static boolean runState;
    private static boolean emergency;
    private static boolean camConnected;

    /* Camera Listener Objects */
    private Socket listenerSocket;
    private ServerSocket listenerServerSocket;
    private CamCmdHandler commandHandler;
    private BufferedInputStream istream;
    private OutputStream ostream;

    private ClientStreamData clientStreamData;

    /* Streaming Server Objects */
    HttpConnection reportConn;


    public static boolean isRunning() {
        return runState;
    }

    public static boolean isEmergency() {
        return emergency;
    }

    public static boolean isCamConnected() {
        return camConnected;
    }



    public EmergencyService() {
        reportConn = new HttpConnection();
        runState = false;
        emergency = false;
        camConnected = false;
        clientStreamData = MyApplication.clientStreamData;
    }


    private final class CamCmdHandler extends Handler {
        public CamCmdHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NotNull Message msg) {
            try {
                /* Get camera info from user settings */
                VideoConfig.update(getApplicationContext());
                JSONObject resp = startStream();
                if (resp.getBoolean("result")) {
                    setupEmergencyStream(resp);
                    setupCamSocket();
                    broadcastStartOfStream();
                } else {
                    //@Todo: error handler
                    return;
                }


                /*
                 *   Wifi-Camera Interface
                 *   --------------------------------------------
                 *   Get requests from tcp socket to wifi camera.
                 *   Perform according actions (set state, send request to streaming server)
                 *   and respond to camera with the results.
                 */
                while (true) {
                    if (runState && (istream.available() > 0)) {
                        byte[] msgBuf = new byte[istream.available()];
                        istream.read(msgBuf, 0, istream.available());
                        if (!isPreamble(msgBuf))
                            continue;
                        handleMsgFromCamera(msgBuf);
                    } else {
                        return;
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMsgFromCamera(byte[] msgBuf) throws IOException, JSONException {

        byte cmd = msgBuf[2];
        final int SERIAL_START_BIT = 3;
        final int SERIAL_LAST_BIT = 9;

        if (cmd == WifiCameraProtocol.CAM_CMD_START_EMERGENCY)
            startEmergency();

        else if (cmd == WifiCameraProtocol.CAM_CMD_STOP_EMERGENCY)
            stopEmergency();

        else if (cmd == WifiCameraProtocol.CAM_CMD_ACTIVATE) {
            String serialNumber = Arrays.toString(Arrays.copyOfRange(msgBuf, SERIAL_START_BIT, SERIAL_LAST_BIT));
            boolean camVerified = VideoConfig.serialNumber.equals(serialNumber);

            if (camVerified) {
                /* Camera serial number matches with application data */
                activateCamera();
            }
            else
                ostream.write(WifiCameraProtocol.CAM_RESP_ERR);
        }
    }


    private JSONObject startEmergency() throws IOException, JSONException {

        if (camConnected) {
            ostream.write(WifiCameraProtocol.CAM_CMD_PREAMBLE);
            ostream.write(WifiCameraProtocol.CAM_RESP_ACK);
            ostream.flush();

            emergency = true;
            JSONObject sendData = new JSONObject();
            sendData.put("token", MyApplication.currentUser.webToken);
            return new JSONObject(
                    reportConn.sendHttpRequest(
                            StreamingURI.URI_EMERGENCY,
                            new JSONObject(),
                            HttpConnection.POST,
                            Addresses.STREAMING_SERVER_IP
                    )
            );
        } else {
            ostream.write(WifiCameraProtocol.CAM_RESP_ERR);
            return null;
        }
    }

    private JSONObject stopEmergency() throws IOException, JSONException {

        JSONObject sendData = new JSONObject();
        sendData.put("token", MyApplication.currentUser.webToken);
        JSONObject result = new JSONObject(
                reportConn.sendHttpRequest(
                    StreamingURI.URI_EMERGENCY,
                    new JSONObject(),
                    HttpConnection.DELETE,
                        Addresses.STREAMING_SERVER_IP
                )
        );

        //@Todo: confirm result before sending message to camera
        {
            // Close success
            ostream.write(WifiCameraProtocol.CAM_CMD_PREAMBLE);
            ostream.write(WifiCameraProtocol.CAM_RESP_ACK);
            ostream.flush();
            emergency = false;
        } // else
        {
            //Close Fail
        }

        return result;
    }

    private JSONObject startStream() throws IOException, JSONException {
        JSONObject streamInfo = new JSONObject();
        streamInfo.put("format", VideoConfig.format);
        streamInfo.put("resolution", VideoConfig.resolution);

        streamInfo.put("token", MyApplication.currentUser.webToken);
        return new JSONObject(
            reportConn.sendHttpRequest(
                StreamingURI.URI_STREAM + '/' + MyApplication.currentUser.username,
                    streamInfo,
                HttpConnection.POST,
                    Addresses.STREAMING_SERVER_IP
            )
        );
    }

    private JSONObject stopStream() throws IOException, JSONException {
        JSONObject data = new JSONObject();
        data.put("token", MyApplication.currentUser.webToken);
        return new JSONObject(
            reportConn.sendHttpRequest(
                StreamingURI.URI_STREAM,
                new JSONObject(),
                HttpConnection.DELETE,
                    Addresses.STREAMING_SERVER_IP
            )
        );
    }

    public void broadcastStartOfStream() {
        /* Notify updated state to main activity for UI update */
        Intent intent = new Intent();
        intent.setAction(EmergencyMessages.START_STREAM);
        intent.putExtra("videoUrl", clientStreamData.getVideoDestUrl());
        intent.putExtra("audioUrl", clientStreamData.getAudioDestUrl());
        intent.putExtra("geoUrl", clientStreamData.getGeoDestUrl());
        sendBroadcast(intent);
    }

    public void setupEmergencyStream(JSONObject resp) throws JSONException {
        clientStreamData.setId(resp.getInt("id"));
        clientStreamData.setVideoDestUrl(resp.getString("videoUrl"));
        clientStreamData.setAudioDestUrl(resp.getString("audioUrl"));
        clientStreamData.setGeoDestUrl(resp.getString("geoUrl"));
    }

    public void setupCamSocket() throws IOException {
        /* Prepare TCP socket to wifi camera */
        listenerServerSocket = new ServerSocket();
        listenerServerSocket.bind(new InetSocketAddress(Addresses.HOTSPOT_HOST_IP, 8001));
        listenerSocket = listenerServerSocket.accept();
        ostream = listenerSocket.getOutputStream();
        istream = new BufferedInputStream(listenerSocket.getInputStream());
    }

    public void activateCamera() throws IOException {
        camConnected = true;
        ostream.write((byte) clientStreamData.getAudioDestUrl().length());
        ostream.write((byte) clientStreamData.getVideoDestUrl().length());
        ostream.write(VideoConfig.resolution);
        ostream.write(VideoConfig.format);
        ostream.write(clientStreamData.getVideoDestUrl().getBytes(StandardCharsets.UTF_8));
        ostream.write(clientStreamData.getAudioDestUrl().getBytes(StandardCharsets.UTF_8));
        ostream.flush();
    }

    private boolean isPreamble(byte[] buf) {
        return buf[0] == WifiCameraProtocol.CAM_CMD_PREAMBLE[0]
                && buf[1] == WifiCameraProtocol.CAM_CMD_PREAMBLE[1];
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!runState) {
            runState = true;
            HandlerThread handlerThread = new HandlerThread("TCP Handler Thread",
                    Process.THREAD_PRIORITY_BACKGROUND);
            handlerThread.start();
            commandHandler = new CamCmdHandler(handlerThread.getLooper());
            commandHandler.sendEmptyMessage(0);
            return START_STICKY;
        }
        else
            return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        /* Stop handler thread */
        runState = false;

        byte[] buf = new byte[1];
        int retries = 0;

        try {
            /* Notify emergency server and camera device */
            stopStream();
            ostream.write(WifiCameraProtocol.CAM_CMD_DISCONNECT);
            ostream.flush();

            while (buf[0] != WifiCameraProtocol.CAM_RESP_ACK) {
                istream.read(buf, 0, 1);
                retries++;
                if (retries == 100)
                    break;
            }

            if (!listenerServerSocket.isClosed())
                listenerServerSocket.close();
            if (!listenerSocket.isClosed())
                listenerSocket.close();
        }catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
