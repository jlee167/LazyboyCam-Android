package com.example.guardiancamera_wifi.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.guardiancamera_wifi.Env;
import com.example.guardiancamera_wifi.MyApplication;
import com.example.guardiancamera_wifi.data.net.http.StreamingServer;
import com.example.guardiancamera_wifi.data.exceptions.RequestDeniedException;
import com.example.guardiancamera_wifi.data.utils.VideoConfig;
import com.example.guardiancamera_wifi.data.utils.WifiCameraProtocol;
import com.example.guardiancamera_wifi.domain.model.EmergencyMessages;
import com.example.guardiancamera_wifi.domain.model.Stream;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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

    private static boolean serviceRunning;
    private static boolean streamRunning;
    private static boolean emergency;
    private static boolean camConnected;
    private static boolean stopRequested;

    private HandlerThread mainThread;

    private Socket listenerSocket;
    private ServerSocket listenerServerSocket;
    private CamCmdHandler commandHandler;
    private BufferedInputStream camInputStream;
    private DataOutputStream camOutputStream;

    private DatagramSocket videoInSock;
    private DatagramSocket audioInSock;
    private Socket videoSendSock;
    private Socket audioSendSock;
    private DatagramSocket audioUdpSock;

    private Location location;
    private LocationManager locationManager;
    public LocationListener locationListener;

    private Stream stream;
    private StreamingServer streamingServer;
    private static VideoConfig videoConfig;

    private ExecutorService executor;

    public final int AUDIO_BUFSIZE_KB = 8;
    public final int AUDIO_BUFSIZE = 1024 * AUDIO_BUFSIZE_KB;


    public static boolean isServiceRunning() {
        return serviceRunning;
    }

    public static boolean isEmergency() {
        return emergency;
    }

    public static boolean isCamConnected() {
        return camConnected;
    }

    public static boolean isStreamRunning() {
        return streamRunning;
    }


    public EmergencyService() {
        serviceRunning = false;
        emergency = false;
        camConnected = false;
        stopRequested = false;
        stream = MyApplication.clientStream;
        streamingServer = MyApplication.streamingServer;
        videoConfig = new VideoConfig(this);
    }


    private final class CamCmdHandler extends Handler {
        public CamCmdHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NotNull Message msg) {

            try {
                videoConfig.update();
                JSONObject streamInfo = startStream();
                streamRunning = true;
                initCameraSocket();
                initLocationManager();
                startLocationBroadcast();
                registerStreamInfo(streamInfo);
                executor = Executors.newFixedThreadPool(6);
            } catch (Exception e) {
                stopSelf();
                return;
            }

            broadcastState(EmergencyMessages.STREAM_READY);




            /*
             *   Wifi-Camera Interface
             *   --------------------------------------------
             *   Get requests from tcp socket to wifi camera.
             *   Perform according actions (set state, send request to streaming server)
             *   and respond to camera with the results.
             */

            try {
                final int maxImgSize = 300 * 1024;
                byte[] videoBuf = new byte[maxImgSize];
                DatagramPacket videoPacket = new DatagramPacket(videoBuf, videoBuf.length);
                Queue<byte[]> sendQueue = new LinkedList<>();

                byte[] audioBuf = new byte[AUDIO_BUFSIZE];
                DatagramPacket audioPacket = new DatagramPacket(audioBuf, audioBuf.length);
                Queue<byte[]> audioQueue = new LinkedList<>();

                byte[] audioSendBuf = new byte[AUDIO_BUFSIZE/2];
                DatagramPacket audioSendPacket = new DatagramPacket(audioSendBuf, audioSendBuf.length);


                String videoIpPort = stream.getVideoPostUrl().split("://")[1];
                int videoPort = Integer.parseInt(videoIpPort.split(":")[1]);
                videoSendSock = new Socket("www.lazyboyindustries.com", videoPort);
                videoSendSock.setSendBufferSize(maxImgSize);
                OutputStream videoOutStream = videoSendSock.getOutputStream();

                String audioPostIpPort = stream.getAudioPostUrl().split("://")[1];
                int audioPort = Integer.parseInt(audioPostIpPort.split(":")[1]);
                audioSendPacket.setSocketAddress(new InetSocketAddress("www.lazyboyindustries.com", audioPort));
                audioSendSock = new Socket("www.lazyboyindustries.com", audioPort);
                audioSendSock.setSoTimeout(500);
                audioSendSock.setSendBufferSize(1024*512);
                audioSendSock.setTcpNoDelay(true);
                OutputStream audioOutStream = audioSendSock.getOutputStream();

                audioUdpSock = new DatagramSocket(8005);



                /* Video sender thread */
                executor.submit(() -> {
                    int frameCnt = 0;
                    while (true) {
                        try {
                                videoInSock.receive(videoPacket);
                                String imgString = Base64.encodeToString(
                                    videoPacket.getData(), 0, videoPacket.getLength(),
                                    Base64.DEFAULT);
                                byte [] data = imgString.getBytes(StandardCharsets.UTF_8);
                                sendQueue.add(data);
                                Log.i("MJPEG", String.valueOf(++frameCnt));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });


                /* Video receiver thread */
                executor.submit(() -> {
                    while (true) {
                        if (!sendQueue.isEmpty()) {
                            byte[] data = sendQueue.remove();
                            try {
                                videoOutStream.write(data, 0, data.length);
                                videoOutStream.flush();
                            } catch (IOException exception) {
                                exception.printStackTrace();
                            }
                        }
                    }
                });


                executor.submit(() -> {
                    while (true) {
                        try {
                            audioInSock.receive(audioPacket);
                            Log.i("Audio", "recv");
                            byte[] recv = audioPacket.getData();
                            Log.i("Audio", "data acq");
                            audioOutStream.write(recv, 0, AUDIO_BUFSIZE);
                            //audioOutStream.flush();
                            Log.i("Audio", "sent");
                        } catch(Exception e) {
                            Log.e("[Audio]", e.getMessage());
                        }
                    }
                });


                /*executor.submit(() -> {
                    while (true) {
                        if (!audioQueue.isEmpty()) {
                            //byte[] data = audioQueue.remove();
                            //audioOutStream.write(data);
                            //audioOutStream.flush();
                            //audioSendPacket.setData(data);
                            //audioSendSock.send(audioSendPacket);
                            Log.i("Audio", "sent");
                        }
                    }
                });*/


                /* Geolocation sender thread */
                executor.submit(() -> {
                    while (true) {
                        Handler handler = new Handler();
                        handler.postDelayed(EmergencyService.this::requestLocation, 2000);
                        handler.wait();
                    }
                });

                while (true) {
                    if (serviceRunning) {
                        if (camInputStream.available() > 0) {
                            byte[] msgBuf = new byte[camInputStream.available()];
                            camInputStream.read(msgBuf, 0, camInputStream.available());
                            if (!isPreamble(msgBuf))
                                continue;
                            try {
                                handleMsgFromCamera(msgBuf);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        if (stopRequested) {
                            try {
                                if (!executor.isShutdown())
                                    executor.shutdown();
                                if (streamRunning) {
                                    stopStream();
                                }
                                stopRequested = false;
                            } catch (RequestDeniedException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "Error occured! Refer to log",
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }


    private void handleMsgFromCamera(byte[] msgBuf) throws IOException, JSONException, RequestDeniedException {

        byte cmd = msgBuf[2];
        final int SERIAL_START_BIT = 3;
        final int SERIAL_LAST_BIT = 11;

        if (!isCamConnected()) {
            if (cmd == WifiCameraProtocol.CAM_CMD_ACTIVATE) {
                String serialNumber = new String(Arrays.copyOfRange(msgBuf, SERIAL_START_BIT, SERIAL_LAST_BIT));
                boolean camVerified = videoConfig.serialNumber.equals(serialNumber);

                if (camVerified) {
                    activateCamera();
                    broadcastState(EmergencyMessages.CAMERA_CONNECTED);
                }
                else
                    camOutputStream.write(WifiCameraProtocol.CAM_RESP_ERR);
            }
            else {
                camOutputStream.write(WifiCameraProtocol.CAM_RESP_ERR);
                return;
            }
        }

        if (cmd == WifiCameraProtocol.CAM_CMD_START_EMERGENCY) {
            startEmergency();
            broadcastState(EmergencyMessages.EMERGENCY_STARTED);
        }

        else if (cmd == WifiCameraProtocol.CAM_CMD_STOP_EMERGENCY) {
            stopEmergency();
            broadcastState(EmergencyMessages.EMERGENCY_STOPPED);
        }
    }


    private void initLocationManager() {
        /* @Todo Error Notification */
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }


    private void startLocationBroadcast() {
        locationListener = location -> {
            try {
                JSONObject locationData = new JSONObject();
                locationData.put("latitude", location.getLatitude());
                locationData.put("longitude", location.getLongitude());
                locationData.put("timestamp", System.currentTimeMillis());
                streamingServer.sendLocation(locationData);
            } catch (JSONException | RequestDeniedException | IOException e) {
                e.printStackTrace();
            }
        };
    }


    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                2000, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                2000, 0, locationListener);
    }


    private JSONObject startEmergency() throws IOException, JSONException, RequestDeniedException {

        if (camConnected) {
            camOutputStream.write(WifiCameraProtocol.CAM_CMD_PREAMBLE);
            camOutputStream.write(WifiCameraProtocol.CAM_RESP_ACK);
            camOutputStream.flush();
            emergency = true;
            return streamingServer.startEmergency();
        } else {
            camOutputStream.write(WifiCameraProtocol.CAM_RESP_ERR);
            return null;
        }
    }


    private JSONObject stopEmergency() throws IOException, JSONException, RequestDeniedException {

        JSONObject responseBody = streamingServer.stopEmergency();

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
        try {
            JSONObject responseBody = streamingServer.startStream(videoConfig);
            if (responseBody.getBoolean("result")) { return responseBody; }
            else { throw new RequestDeniedException();}
        } catch (RequestDeniedException e){
            Toast.makeText(getBaseContext(), "Authentication failed!",
                    Toast.LENGTH_LONG).show();
            throw e;
        } catch (ConnectException e) {
            Toast.makeText(getBaseContext(), "Connection to streaming server failed!",
                    Toast.LENGTH_LONG).show();
            throw e;
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), "Unexpected error! " + e.getClass().getName(),
                    Toast.LENGTH_LONG).show();
            throw e;
        }
    }


    private void stopStream() throws IOException, JSONException, RequestDeniedException {
        try {
            JSONObject responseBody = streamingServer.stopStream();
            boolean success = responseBody.getBoolean("result");
            if (!success)
                throw new RequestDeniedException();
            else
                streamRunning = false;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    public void broadcastState(String state) {
        Intent intent = new Intent();
        intent.setAction(state);
        intent.putExtra("videoUrl", stream.getVideoPostUrl());
        intent.putExtra("audioUrl", stream.getAudioPostUrl());
        intent.putExtra("geoUrl", stream.getGeoDataPostUrl());
        sendBroadcast(intent);
    }


    public void registerStreamInfo(JSONObject resp) throws JSONException {
        stream.setId(resp.getInt("id"));
        stream.setVideoPostUrl(resp.getString("videoUrl"));
        stream.setAudioPostUrl(resp.getString("audioUrl"));
        stream.setGeoDataPostUrl(resp.getString("geoLocationUrl"));
    }


    public void initCameraSocket() throws IOException {
        /* Prepare TCP socket to wifi camera */
        try {
            connectToCamera();
            openMediaInputSockets();
        } catch (ConnectException e) {
            Toast.makeText(getBaseContext(), "Camera socket (tcp) initialization failed!",
                    Toast.LENGTH_LONG).show();
            throw e;
        } catch (BindException e) {
            Toast.makeText(getBaseContext(), "Please turn on wifi hotspot first",
                    Toast.LENGTH_LONG).show();
            throw e;
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), "Unexpected error! " + e.getClass().getName(),
                    Toast.LENGTH_LONG).show();
            throw e;
        }
    }


    public void connectToCamera() throws IOException {
        listenerServerSocket = new ServerSocket();
        listenerServerSocket.bind(new InetSocketAddress(Env.HOTSPOT_HOST_IP, 8001));
        listenerSocket = listenerServerSocket.accept();
        camOutputStream = new DataOutputStream(listenerSocket.getOutputStream());
        camInputStream = new BufferedInputStream(listenerSocket.getInputStream());
    }


    public void openMediaInputSockets() throws SocketException {
        videoInSock = new DatagramSocket(8002);
        audioInSock = new DatagramSocket(8003);
        audioInSock.setSoTimeout(500);
    }


    public void activateCamera() throws IOException {
        camConnected = true;
        camOutputStream.write((byte) stream.getVideoPostUrl().length());
        camOutputStream.write(stream.getVideoPostUrl().getBytes(StandardCharsets.UTF_8));
        camOutputStream.write((byte) stream.getAudioPostUrl().length());
        camOutputStream.write(stream.getAudioPostUrl().getBytes(StandardCharsets.UTF_8));
        camOutputStream.write((byte) stream.getGeoDataPostUrl().length());
        camOutputStream.write(stream.getGeoDataPostUrl().getBytes(StandardCharsets.UTF_8));
        camOutputStream.write((byte)MyApplication.currentUser.getStreamAccessToken().length());
        camOutputStream.write(MyApplication.currentUser.getStreamAccessToken().getBytes(StandardCharsets.UTF_8));
    }


    private boolean isPreamble(byte[] buf) {
        return buf[0] == WifiCameraProtocol.CAM_CMD_PREAMBLE[0]
                && buf[1] == WifiCameraProtocol.CAM_CMD_PREAMBLE[1];
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!serviceRunning) {
            serviceRunning = true;
            mainThread = new HandlerThread("TCP Handler Thread",
                    Process.THREAD_PRIORITY_BACKGROUND);
            mainThread.start();
            commandHandler = new CamCmdHandler(mainThread.getLooper());
            commandHandler.sendEmptyMessage(0);
            return START_STICKY;
        }
        else {
            return START_NOT_STICKY;
        }
    }


    @Override
    public void onDestroy() {

        stopRequested = true;

        {
            int retries = 0;
            int timeout = 0;
            while (isStreamRunning()) {
                timeout++;
                if (timeout > 1000000) {
                    retries++;
                    if (retries >= 5) {
                        CharSequence text = "Service could not be stopped safely. Forcing stop.";
                        Toast.makeText(this.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                        break;
                    }
                    timeout = 0;
                }
            }
        }

        /* Destroy threads created in service */
        if (executor == null) {}
        else if (!executor.isShutdown()) {executor.shutdownNow();}
        mainThread.quitSafely();



        try {
            /* Notify emergency server and camera device */
            /*
            camOutputStream.write(WifiCameraProtocol.CAM_CMD_DISCONNECT);
            camOutputStream.flush();

            while (buf[0] != WifiCameraProtocol.CAM_RESP_ACK) {
                camInputStream.read(buf, 0, 1);
                retries++;
                if (retries == 100) {
                    CharSequence text = "Camera could not be stopped. Please restart camera.";
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                    break;
                }
            }
            */

            if (!listenerServerSocket.isClosed())
                listenerServerSocket.close();
            if (!listenerSocket.isClosed())
                listenerSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent().setAction(EmergencyMessages.STREAM_STOPPED);
        sendBroadcast(intent);

        serviceRunning = false;
        streamRunning = false;
        super.onDestroy();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}