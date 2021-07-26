
//package com.example.guardiancamera_wifi;
//
//import android.Manifest;
//import android.annotation.SuppressLint;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
//import java.net.InetAddress;
//import java.net.SocketException;
//import java.net.UnknownHostException;
//import java.util.ArrayDeque;
//import java.util.Arrays;
//import java.util.concurrent.Semaphore;
//
//
//public class TrackingActivity extends AppCompatActivity {
//    DatagramSocket mjpegCamSocket, serverSocket;
//    DatagramPacket image_packet, send_packet;
//    byte [] data;
//    Thread camThread, serverThread;
//    ArrayDeque<DatagramPacket> packetQueue = new ArrayDeque<DatagramPacket>();
//    Semaphore packetQueueSemaphore = new Semaphore(1);
//    TextView outtext;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_tracking);
//
//        requestPermissions(new String[]{Manifest.permission.INTERNET}, 1);
//
//        final String IP_HOST = "192.168.43.1";
//        final String IP_CAM = "192.168.43.74";
//        final String IP_SERVER = "192.168.43.74";
//        data = new byte[1024];
//        outtext = findViewById(R.id.OUTTEXT);
//
//
//        final Handler handler = new Handler(){
//            public void handleMessage(Message msg){
//                outtext.setText(msg.getData().getString("Key"));
//            }
//        };
//
//
//        camThread = new Thread(){
//          @Override
//          public void run() {
//              try {
//                  mjpegCamSocket.connect(InetAddress.getByName(IP_CAM),8000);
//                  image_packet = new DatagramPacket(data,0,1024);
//                  while(true) {
//                      try {
//                          mjpegCamSocket.receive(image_packet);
//                          packetQueueSemaphore.acquire();
//                          packetQueue.add(image_packet);
//                          packetQueueSemaphore.release();
//
//                          Message msg = handler.obtainMessage();
//                          Bundle data = new Bundle();
//                          data.putString("Key", new String(image_packet.getData(), "UTF-8"));
//                          msg.setData(data);
//                          handler.sendMessage(msg);
//
//                      } catch (IOException | InterruptedException e) {
//                          e.printStackTrace();
//                      }
//                  }
//              } catch (UnknownHostException e) {
//                  e.printStackTrace();
//              }
//          }
//        };
//
//        serverThread = new Thread(){
//            @Override
//            public void run() {
//                try {
//                    serverSocket.connect(InetAddress.getByName(IP_SERVER),8001);
//                    send_packet = new DatagramPacket(data,0,1024);
//                    while(true) {
//                        while (!packetQueue.isEmpty()) {
//                            try {
//                                packetQueueSemaphore.acquire();
//                                //send_packet = packetQueue.pop();
//                                packetQueueSemaphore.release();
//                                serverSocket.send(send_packet);
//                            } catch (IOException | InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//
//        try {
//            mjpegCamSocket = new DatagramSocket(8000, InetAddress.getByName(IP_HOST));
//            camThread.start();
//        } catch (SocketException | UnknownHostException e) {
//            e.printStackTrace();
//        }
//
//
//        try {
//            serverSocket = new DatagramSocket(8001, InetAddress.getByName(IP_HOST));
//            serverThread.start();
//        } catch (SocketException | UnknownHostException e) {
//            e.printStackTrace();
//        }
//
//
//    }
//}