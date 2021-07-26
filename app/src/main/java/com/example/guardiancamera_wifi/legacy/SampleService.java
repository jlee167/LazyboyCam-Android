package com.example.guardiancamera_wifi.legacy;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.example.guardiancamera_wifi.models.MyApplication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.concurrent.Semaphore;


public class SampleService extends Service {

    private static final int RETURN_CODE_SUCCESS = 0x00;
    private static final int RETURN_CODE_CANCEL = 0x01;

    // Running status of the service. True when one or more instance is running.
    // There should not be more than one instance running concurrently!
    private static boolean runState;


    // Stream TCP variables: sockets and buffers
    private Socket mjpegTcpSocket, serverTcpSocket;
    private ServerSocket mjpegAcceptSocket, serverAcceptSocket;
    private BufferedReader mjpegTcpInput;
    private PrintWriter mjpegTcpOutput;
    byte tcp_buf[];

    // Command Channel TCP variables: sockets and buffers
    private Socket camCommandSocket;
    OutputStream camCmdOutput;
    PrintWriter output;
    InputStream camCmdInput;

    // UDP Stream variables. Currently unused
    private DatagramSocket mjpegCamSocket, serverSocket;
    private DatagramPacket image_packet, server_packet;
    private byte [] data;
    private Thread camThread, serverThread;
    SocketAddress mjpegAddr;
    SocketAddress serverAddr;

    // Cam to server FIFO Buffer
    private ArrayDeque<DatagramPacket> packetQueue = new ArrayDeque<DatagramPacket>();

    private Semaphore packetQueueSemaphore = new Semaphore(1);
    private StreamHandler streamHandler;
    private Looper serviceLooper;

    // IP addresses and ports for Self (Android Device with hotpost), Camera, and Streaming Server
    private final String IP_HOST = "192.168.43.1";
    private final String IP_CAM = "192.168.43.74";
    private final String IP_SERVER = "192.168.43.74";
    private final int PORT_COMMAND = 8000;
    private final int PORT_STREAM_IN = 8001;
    private final int PORT_STREAM_OUT = 8002;

    private final byte CAM_CMD_START_VIDEO = 0x40;
    private final byte CAM_CMD_STOP_VIDEO = 0x41;
    private final byte CAM_CMD_REQUEST_SERIAL_ID = 0x42;
    private final byte CAM_CMD_SET_FRAMESIZE = 0X43;

    private int rc;


    /**
     * @return
     *      True if the service is running.
     */
    public static boolean isRunning() {
        return runState;
    }


    /**
     *  Default Constructor
     *  Do nothing.
     */
    public SampleService() {
    }


    /**
     * Message handler for this service.
     */
    private final class StreamHandler extends Handler {

        public StreamHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            /*
            try {
                mjpegCamSocket = new DatagramSocket(PORT_STREAM_IN, InetAddress.getByName(IP_HOST));
            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
                stopSelf();
            }

            try {
                serverSocket = new DatagramSocket(PORT_STREAM_OUT, InetAddress.getByName(IP_HOST));
            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
                stopSelf();
            }
            */

            try {
                serverAcceptSocket = new ServerSocket();
                serverAcceptSocket.bind(new InetSocketAddress(IP_HOST,PORT_STREAM_OUT));
                //serverTcpSocket = new Socket();
                //serverTcpSocket.bind(new InetSocketAddress(IP_HOST,PORT_STREAM_OUT));
                mjpegAcceptSocket = new ServerSocket();
                mjpegAcceptSocket.bind(new InetSocketAddress(IP_HOST,PORT_STREAM_IN));
                mjpegTcpSocket = mjpegAcceptSocket.accept();
                serverTcpSocket = serverAcceptSocket.accept();
                //serverTcpSocket.connect(serverAddr);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            //sends the message to the server
            try {
                mjpegTcpOutput = new PrintWriter(new BufferedWriter(new OutputStreamWriter(serverTcpSocket.getOutputStream())), true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //receives the message which the server sends back
            try {
                mjpegTcpInput = new BufferedReader(new InputStreamReader(mjpegTcpSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            camThread.start();
            //serverThread.start();
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        /*
        try {
            camCommandSocket = new Socket(IP_HOST, PORT_COMMAND);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        */

        data = new byte[60240];
        mjpegAddr = new InetSocketAddress(IP_CAM,PORT_STREAM_IN);
        serverAddr = new InetSocketAddress(IP_SERVER,PORT_STREAM_OUT);

        tcp_buf = new byte[10];

        camThread = new Thread(){
            @Override
            public void run() {
                try {
                    while (true) {
                        if (mjpegTcpSocket.getInputStream().available() > 0)
                            serverTcpSocket.getOutputStream().write(mjpegTcpSocket.getInputStream().read());
                    }
                    //String message = mjpegTcpInput.readLine();
                    //serverTcpSocket.getOutputStream().write(message.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };


        /*
        data = new byte[60240];
        try {
            image_packet = new DatagramPacket(data,0,60240, InetAddress.getByName(IP_CAM), PORT_STREAM_IN);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            server_packet = new DatagramPacket(data,0,60240, InetAddress.getByName(IP_SERVER), PORT_STREAM_OUT);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        };
        camThread = new Thread(){
            @Override
            public void run() {
                //mjpegCamSocket.connect(InetAddress.getByName(IP_CAM),PORT_STREAM_IN);

                while(true) {
                    try {
                        mjpegCamSocket.receive(image_packet);
                        packetQueueSemaphore.acquire();
                        packetQueue.add(image_packet);
                        packetQueueSemaphore.release();
                        Log.i("KAKAO_SESSION", Arrays.toString(image_packet.getData()));
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        serverThread = new Thread(){
            @Override
            public void run() {
                try {
                    serverSocket.connect(InetAddress.getByName(IP_SERVER),PORT_STREAM_OUT);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                while(true) {
                    if (!packetQueue.isEmpty()) {
                        try {
                            packetQueueSemaphore.acquire();
                            server_packet.setData(packetQueue.pop().getData());
                            Log.i("Server", Arrays.toString(server_packet.getData()));
                            //server_packet = packetQueue.pop();
                            //server_packet.setAddress(InetAddress.getByName(IP_SERVER));
                            //server_packet.setPort(PORT_STREAM_OUT);
                            serverSocket.send(server_packet);
                            packetQueueSemaphore.release();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        */

        HandlerThread handlerThread = new HandlerThread("Streaming Service Handler", Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();

        serviceLooper = handlerThread.getLooper();
        streamHandler = new StreamHandler(serviceLooper);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        /*
        try {
            camCommandSocket.connect(new InetSocketAddress(IP_CAM, PORT_COMMAND));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        */

        Message msg = streamHandler.obtainMessage();
        msg.arg1 = startId;
        streamHandler.sendMessage(msg);

        MyApplication.postApplicationLog("Capture Service Started...");

        runState = true;
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        try {
            camCmdOutput.write(CAM_CMD_STOP_VIDEO);
        } catch (IOException e) {
            e.printStackTrace();
        }
        FFmpeg.cancel();

        try {
            mjpegTcpSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            serverTcpSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        runState = false;
        super.onDestroy();
    }
}
