//package com.example.guardiancamera_wifi;
//
//import androidx.annotation.RequiresApi;
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.Manifest;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.net.wifi.ScanResult;
//import android.net.wifi.p2p.WifiP2pDevice;
//import android.net.wifi.p2p.WifiP2pDeviceList;
//import android.net.wifi.p2p.WifiP2pManager;
//import android.os.Build;
//import android.os.Bundle;
//import android.net.wifi.WifiManager;
//import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
//import android.util.Log;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ReserveWifiHotspotActivity extends AppCompatActivity {
//
//    WifiManager manager;
//    WifiP2pManager.Channel channel;
//    WifiManager.LocalOnlyHotspotCallback hotspotCallback;
//
//
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main_menu);
//
//        manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
//        hotspotCallback = new WifiManager.LocalOnlyHotspotCallback(){
//            @Override
//            public void onFailed(int reason) {
//                super.onFailed(reason);
//            }
//
//            @Override
//            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
//                super.onStarted(reservation);
//            }
//
//            @Override
//            public void onStopped() {
//                super.onStopped();
//            }
//        };
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    @Override
//    protected void onStart() {
//        super.onStart();
//        manager.startLocalOnlyHotspot(hotspotCallback, null);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//    }
//}
