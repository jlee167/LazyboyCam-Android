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

import com.example.guardiancamera_wifi.MyApplication;
import com.example.guardiancamera_wifi.data.api.http.UserEmergencyConnection;
import com.example.guardiancamera_wifi.data.api.http.exceptions.RequestDeniedException;
import com.example.guardiancamera_wifi.data.configs.AndroidHotspot;
import com.example.guardiancamera_wifi.data.configs.VideoConfig;
import com.example.guardiancamera_wifi.data.configs.WifiCameraProtocol;
import com.example.guardiancamera_wifi.domain.models.ClientStreamInfo;
import com.example.guardiancamera_wifi.domain.models.EmergencyMessages;

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

    private Socket listenerSocket;
    private ServerSocket listenerServerSocket;
    private CamCmdHandler commandHandler;
    private BufferedInputStream camInputStream;
    private OutputStream camOutputStream;
    private ClientStreamInfo streamInfo;
    private UserEmergencyConnection userEmergencyConnection;
    private static VideoConfig videoConfig;


    public static boolean isRunning() {
        return runState;
    }

    public static boolean isEmergency() {
        return emergency;
    }

    public static boolean isCamConnected() {
        return camConnected;
    }

    public static VideoConfig getVideoConfig() {
        return videoConfig;
    }



    public EmergencyService() {
        runState = false;
        emergency = false;
        camConnected = false;
        streamInfo = MyApplication.clientStreamInfo;
        userEmergencyConnection = MyApplication.userEmergencyConnection;
        videoConfig = new VideoConfig(getApplicationContext());
    }


    private final class CamCmdHandler extends Handler {
        public CamCmdHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NotNull Message msg) {
            try {
                videoConfig.update();
                JSONObject resp = startStream();
                registerStreamInfo(resp);
                initCameraSocket();
                broadcastState(EmergencyMessages.STREAM_READY);

                /*
                 *   Wifi-Camera Interface
                 *   --------------------------------------------
                 *   Get requests from tcp socket to wifi camera.
                 *   Perform according actions (set state, send request to streaming server)
                 *   and respond to camera with the results.
                 */
                while (true) {
                    if (runState && (camInputStream.available() > 0)) {
                        byte[] msgBuf = new byte[camInputStream.available()];
                        camInputStream.read(msgBuf, 0, camInputStream.available());
                        if (!isPreamble(msgBuf))
                            continue;
                        handleMsgFromCamera(msgBuf);
                    } else {
                        return;
                    }
                }
            } catch (IOException | JSONException | RequestDeniedException e) {
                e.printStackTrace();
            }
        }
    }


    private void handleMsgFromCamera(byte[] msgBuf) throws IOException, JSONException, RequestDeniedException {

        byte cmd = msgBuf[2];
        final int SERIAL_START_BIT = 3;
        final int SERIAL_LAST_BIT = 9;

        if (cmd == WifiCameraProtocol.CAM_CMD_START_EMERGENCY) {
            startEmergency();
            broadcastState(EmergencyMessages.EMERGENCY_STARTED);
        }

        else if (cmd == WifiCameraProtocol.CAM_CMD_STOP_EMERGENCY) {
            stopEmergency();
            broadcastState(EmergencyMessages.EMERGENCY_STOPPED);
        }

        else if (cmd == WifiCameraProtocol.CAM_CMD_ACTIVATE) {
            String serialNumber = Arrays.toString(Arrays.copyOfRange(msgBuf, SERIAL_START_BIT, SERIAL_LAST_BIT));
            boolean camVerified = videoConfig.serialNumber.equals(serialNumber);

            if (camVerified) {
                activateCamera();
                broadcastState(EmergencyMessages.CAMERA_CONNECTED);
            }
            else
                camOutputStream.write(WifiCameraProtocol.CAM_RESP_ERR);
        }
    }


    private JSONObject startEmergency() throws IOException, JSONException, RequestDeniedException {

        if (camConnected) {
            camOutputStream.write(WifiCameraProtocol.CAM_CMD_PREAMBLE);
            camOutputStream.write(WifiCameraProtocol.CAM_RESP_ACK);
            camOutputStream.flush();
            emergency = true;
            return userEmergencyConnection.startEmergency();
        } else {
            camOutputStream.write(WifiCameraProtocol.CAM_RESP_ERR);
            return null;
        }
    }


    private JSONObject stopEmergency() throws IOException, JSONException, RequestDeniedException {

        JSONObject responseBody = userEmergencyConnection.stopEmergency();

        //@Todo: confirm result before sending message to camera
        {
            // Close success
            camOutputStream.write(WifiCameraProtocol.CAM_CMD_PREAMBLE);
            camOutputStream.write(WifiCameraProtocol.CAM_RESP_ACK);
            camOutputStream.flush();
            emergency = false;
        } // else
        {
            //Close Fail
        }

        return responseBody;
    }


    private JSONObject startStream() throws IOException, JSONException, RequestDeniedException {
        JSONObject responseBody = userEmergencyConnection.startStream(videoConfig);
        if (responseBody.getBoolean("result"))
            return responseBody;
        else
            throw new RequestDeniedException();
    }


    private void stopStream() throws IOException, JSONException, RequestDeniedException {
        JSONObject responseBody = userEmergencyConnection.stopStream();
        boolean success = responseBody.getBoolean("result");
        if (success)
            runState = false;
        else
            throw new RequestDeniedException();
    }


    public void broadcastState(String state) {
        Intent intent = new Intent();
        intent.setAction(state);
        intent.putExtra("videoUrl", streamInfo.getVideoDestUrl());
        intent.putExtra("audioUrl", streamInfo.getAudioDestUrl());
        intent.putExtra("geoUrl", streamInfo.getGeoDestUrl());
        sendBroadcast(intent);
    }


    public void registerStreamInfo(JSONObject resp) throws JSONException {
        streamInfo.setId(resp.getInt("id"));
        streamInfo.setVideoDestUrl(resp.getString("videoUrl"));
        streamInfo.setAudioDestUrl(resp.getString("audioUrl"));
        streamInfo.setGeoDestUrl(resp.getString("geoUrl"));
    }


    public void initCameraSocket() throws IOException {
        /* Prepare TCP socket to wifi camera */
        listenerServerSocket = new ServerSocket();
        listenerServerSocket.bind(new InetSocketAddress(AndroidHotspot.HOTSPOT_HOST_IP, 8001));
        listenerSocket = listenerServerSocket.accept();
        camOutputStream = listenerSocket.getOutputStream();
        camInputStream = new BufferedInputStream(listenerSocket.getInputStream());
    }


    public void activateCamera() throws IOException {
        camConnected = true;
        camOutputStream.write((byte) streamInfo.getAudioDestUrl().length());
        camOutputStream.write((byte) streamInfo.getVideoDestUrl().length());
        camOutputStream.write(videoConfig.resolution);
        camOutputStream.write(videoConfig.format);
        camOutputStream.write(streamInfo.getVideoDestUrl().getBytes(StandardCharsets.UTF_8));
        camOutputStream.write(streamInfo.getAudioDestUrl().getBytes(StandardCharsets.UTF_8));
        camOutputStream.flush();
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
        else {
            return START_NOT_STICKY;
        }
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
            camOutputStream.write(WifiCameraProtocol.CAM_CMD_DISCONNECT);
            camOutputStream.flush();

            while (buf[0] != WifiCameraProtocol.CAM_RESP_ACK) {
                camInputStream.read(buf, 0, 1);
                retries++;
                if (retries == 100)
                    break;
            }

            if (!listenerServerSocket.isClosed())
                listenerServerSocket.close();
            if (!listenerSocket.isClosed())
                listenerSocket.close();
        } catch (IOException | JSONException | RequestDeniedException e) {
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
