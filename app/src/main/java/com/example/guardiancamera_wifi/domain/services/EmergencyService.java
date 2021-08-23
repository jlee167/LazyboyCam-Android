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
import com.example.guardiancamera_wifi.domain.models.EmergencyStream;
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

    private static boolean runState = false;
    private static boolean emergency = false;
    private static boolean camConnected = false;

    /* Camera Listener Objects */
    private Socket listenerSocket;
    private ServerSocket listenerServerSocket;
    CamCmdHandler commandHandler;
    private BufferedInputStream istream;
    private OutputStream ostream;

    /* Streaming Server Objects */
    HttpConnection reportConn;


    public static boolean isRunning() {
        return runState;
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

                /* Send stream start request to streaming server */
                JSONObject streamInfo = new JSONObject();
                streamInfo.put("format", VideoConfig.format);
                streamInfo.put("resolution", VideoConfig.resolution);
                JSONObject resp = startStream(streamInfo);

                if (!resp.getBoolean("result"))
                    return;

                EmergencyStream.setId(resp.getInt("id"));
                EmergencyStream.setVideoDestUrl(resp.getString("videoUrl"));
                EmergencyStream.setAudioDestUrl(resp.getString("audioUrl"));
                EmergencyStream.setGeoDestUrl(resp.getString("geoUrl"));

                /* Prepare TCP socket to wifi camera */
                listenerServerSocket = new ServerSocket();
                listenerServerSocket.bind(new InetSocketAddress(Addresses.HOTSPOT_HOST_IP, 8001));
                listenerSocket = listenerServerSocket.accept();
                ostream = listenerSocket.getOutputStream();
                istream = new BufferedInputStream(listenerSocket.getInputStream());

                /* Notify updated state to main activity for UI update */
                Intent broadcastStreamInfo = new Intent();
                broadcastStreamInfo.setAction("stream.start");
                broadcastStreamInfo.putExtra("videoUrl", EmergencyStream.getVideoDestUrl());
                broadcastStreamInfo.putExtra("audioUrl", EmergencyStream.getAudioDestUrl());
                broadcastStreamInfo.putExtra("geoUrl", EmergencyStream.getGeoDestUrl());
                sendBroadcast(broadcastStreamInfo);




                /*
                 *   Wifi-Camera Interface
                 *   --------------------------------------------
                 *   Get requests from tcp socket to wifi camera.
                 *   Perform according actions (set state, send request to streaming server)
                 *   and respond to camera with the results.
                 */
                while (true) {
                    if (runState && (istream.available() > 0)) {
                        byte[] buf = new byte[istream.available()];
                        int bytesRead = istream.read(buf, 0, istream.available());
                        if (!isPreamble(buf))
                            continue;

                        if (buf[2] == WifiCameraProtocol.CAM_CMD_START_EMERGENCY) {
                            if (camConnected) {
                                ostream.write(WifiCameraProtocol.CAM_CMD_PREAMBLE);
                                ostream.write(WifiCameraProtocol.CAM_RESP_ACK);
                                ostream.flush();
                                startEmergency();
                            } else
                                ostream.write(WifiCameraProtocol.CAM_RESP_ERR);
                        }

                        else if (buf[2] == WifiCameraProtocol.CAM_CMD_STOP_EMERGENCY) {
                            ostream.write(WifiCameraProtocol.CAM_CMD_PREAMBLE);
                            ostream.write(WifiCameraProtocol.CAM_RESP_ACK);
                            ostream.flush();
                            stopEmergency();
                        }

                        else if (buf[2] == WifiCameraProtocol.CAM_CMD_REGISTER) {
                            /* Camera serial number matches */
                            boolean camVerified = VideoConfig.serialNumber.equals(
                                    Arrays.toString(Arrays.copyOfRange(buf, 3, 9))
                            );

                            if (camVerified) {
                                camConnected = true;
                                ostream.write((byte)EmergencyStream.getAudioDestUrl().length());
                                ostream.write((byte)EmergencyStream.getVideoDestUrl().length());
                                ostream.write(VideoConfig.resolution);
                                ostream.write(VideoConfig.format);
                                ostream.write(EmergencyStream.getVideoDestUrl().getBytes(StandardCharsets.UTF_8));
                                ostream.write(EmergencyStream.getAudioDestUrl().getBytes(StandardCharsets.UTF_8));
                                ostream.flush();
                            } else
                                ostream.write(WifiCameraProtocol.CAM_RESP_ERR);
                        }
                    } else {
                        return;
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }


    public EmergencyService() {
        reportConn = new HttpConnection();
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


    private JSONObject startEmergency() throws IOException, JSONException {
        emergency = true;
        JSONObject data = new JSONObject();
        data.put("token", MyApplication.currentUser.webToken);
        return new JSONObject(
                reportConn.sendHttpRequest(
                    StreamingURI.URI_EMERGENCY,
                    new JSONObject(),
                    HttpConnection.POST,
                    Addresses.STREAMING_SERVER_IP
                )
        );
    }

    private JSONObject stopEmergency() throws IOException, JSONException {
        emergency = false;
        JSONObject data = new JSONObject();
        data.put("token", MyApplication.currentUser.webToken);
        return new JSONObject(
                reportConn.sendHttpRequest(
                    StreamingURI.URI_EMERGENCY,
                    new JSONObject(),
                    HttpConnection.DELETE,
                        Addresses.STREAMING_SERVER_IP
                )
        );
    }

    private JSONObject startStream(JSONObject data) throws IOException, JSONException {
        data.put("token", MyApplication.currentUser.webToken);
        return new JSONObject(
            reportConn.sendHttpRequest(
                StreamingURI.URI_STREAM + '/' + MyApplication.currentUser.username,
                data,
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


    private boolean isPreamble(byte[] buf) {
        return buf[0] == WifiCameraProtocol.CAM_CMD_PREAMBLE[0]
                && buf[1] == WifiCameraProtocol.CAM_CMD_PREAMBLE[1];
    }


    @Override
    public void onDestroy() {
        /* Stop handler thread */
        runState = false;

        byte[] buf = new byte[1];
        int retries = 0;

        try {
            /* Notify end of stream to streaming server */
            stopStream();

            /* Notify camera of the end of stream */
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
