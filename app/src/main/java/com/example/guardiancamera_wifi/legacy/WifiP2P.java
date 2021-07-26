//package com.example.guardiancamera_wifi;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.net.wifi.p2p.WifiP2pDevice;
//import android.net.wifi.p2p.WifiP2pDeviceList;
//import android.net.wifi.p2p.WifiP2pManager;
//import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
//import android.util.Log;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class WifiP2P extends BroadcastReceiver {
//
//    private WifiP2pManager mManager;
//    private WifiP2pManager.Channel mChannel;
//    private MainMenuActivity mActivity;
//    PeerListListener myPeerListListener;
//    private List<WifiP2pDevice> peerList = new ArrayList<WifiP2pDevice>();
//
//
//
//    public WifiP2P(WifiP2pManager manager, WifiP2pManager.Channel channel, ReserveWifiP2pActivity activity) {
//        super();
//        this.mManager = manager;
//        this.mChannel = channel;
//        //this.mActivity = activity;
//        this.myPeerListListener = new PeerListListener(){
//            @Override
//            public void onPeersAvailable(WifiP2pDeviceList peers) {
//                List<WifiP2pDevice> refreshedPeers = (List<WifiP2pDevice>) peers.getDeviceList();
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
//                while(peerList.size() == 0) {
//                    Log.d("", "No devices found");
//                    return;
//                }
//
//            }
//        };
//    }
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        String action = intent.getAction();
//
//        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
//            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
//            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
//
//            } else {
//                throw new UnsupportedOperationException("Not yet implemented");
//            }
//        }
//
//        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
//            // Check to see if Wi-Fi is enabled and notify appropriate activity
//        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
//                // request available peers from the wifi p2p manager. This is an
//                // asynchronous call and the calling activity is notified with a
//                // callback on PeerListListener.onPeersAvailable()
//                if (mManager != null) {
//                    mManager.requestPeers(mChannel, myPeerListListener);
//                }
//                Log.d("PEER COUNT", String.valueOf(peerList.size()));
//        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
//            // Respond to new connection or disconnections
//        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
//            // Respond to this device's wifi state changing
//        }
//    }
//}
