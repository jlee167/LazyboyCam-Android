//package com.example.guardiancamera_wifi;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.IntentFilter;
//import android.net.wifi.p2p.WifiP2pDevice;
//import android.net.wifi.p2p.WifiP2pDeviceList;
//import android.net.wifi.p2p.WifiP2pManager;
//import android.os.Bundle;
//import android.util.Log;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ReserveWifiP2pActivity extends AppCompatActivity {
//
//    WifiP2pManager manager;
//    WifiP2pManager.Channel channel;
//    BroadcastReceiver receiver;
//    IntentFilter intentFilter;
//    WifiP2pManager.PeerListListener myPeerListListener;
//    private List<WifiP2pDevice> peerList = new ArrayList<WifiP2pDevice>();
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main_menu);
//
//        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
//        channel = manager.initialize(this, getMainLooper(), null);
//        receiver = new WifiP2P(manager, channel, this);
//
//        intentFilter = new IntentFilter();
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
//        registerReceiver(receiver, intentFilter);
//
//
//        this.myPeerListListener = new WifiP2pManager.PeerListListener() {
//            @Override
//            public void onPeersAvailable(WifiP2pDeviceList peers) {
//                List<WifiP2pDevice> refreshedPeers = new ArrayList<WifiP2pDevice>(peers.getDeviceList());
//                if (!refreshedPeers.equals(peerList)) {
//                    peerList.clear();
//                    peerList.addAll(refreshedPeers);
//
//                    // If an AdapterView is backed by this data, notify it
//                    // of the change. For instance, if you have a ListView of
//                    // available peers, trigger an update.
//                    //((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
//
//                    // Perform any other updates needed based on the new list of
//                    // peers connected to the Wi-Fi P2P network.
//                }
//
//                Log.d("PEER COUNT", String.valueOf(peerList.size()));
//
//                if (peerList.size() == 0) {
//                    Log.d("", "No devices found");
//                    return;
//                }
//
//            }
//        };
//
//
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
//            @Override
//            public void onSuccess() {
//                Log.d("Success", String.valueOf(peerList.size()));
//            }
//
//            @Override
//            public void onFailure(int reasonCode) {
//                Log.d("Fail", String.valueOf(peerList.size()));
//            }
//        });
//        if (manager != null) {
//            manager.requestPeers(channel, myPeerListListener);
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        registerReceiver(receiver, intentFilter);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        unregisterReceiver(receiver);
//    }
//}
