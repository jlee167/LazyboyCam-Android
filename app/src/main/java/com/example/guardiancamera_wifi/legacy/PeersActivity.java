package com.example.guardiancamera_wifi.legacy;

//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import org.json.JSONException;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//
//
///**
// *  Adapter for the recyclerview displaying list of peers.
// */

//class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.mViewHolder> {
//
//    // Dataset Array containing peer info objects
//    ArrayList<LazyWebUser> dataset;
//
//
//    public RecyclerViewAdapter() {
//        dataset = new ArrayList<LazyWebUser>();
//    }
//
//
//    public RecyclerViewAdapter(ArrayList<LazyWebUser> inputDataset) {
//       dataset = new ArrayList<LazyWebUser>();
//       dataset.addAll(inputDataset);
//    }
//
//
//    public void addItem(LazyWebUser dataIn) {
//        dataset.add(dataIn);
//    }
//
//
//    @Override
//    public int getItemCount() {
//        return dataset.size();
//    }
//
//
//    /**
//     *  View Holder format for recyclerview
//     *  Consists of profile picture, personal information, and streaming address
//     */
//    static class mViewHolder extends  RecyclerView.ViewHolder {
//
//        // Child indexes of views within corresponding layouts.
//        static int INDEX_PROFILE_PICTURE = 0;
//        static int INDEX_PERSONAL_INFO = 1;
//        static int INDEX_STATUS = 2;
//        static int INDEX_NAME = 0;
//        static int INDEX_SERVER_ADDRESS = 1;
//
//        View userInfoView;
//
//        mViewHolder(@NonNull View itemView) {
//            super(itemView);
//            userInfoView = itemView;
//        }
//    }
//
//
//    /**
//     * Generate a view holder for recyclerview
//     *
//     * @param parent    -   View that will be used for each list
//     * @param viewType  -
//     * @return
//     *      Viewholder with a view item generated from 'recycleritem' layout
//     */
//    @NonNull
//    @Override
//    public mViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleritem, parent, false);
//        return new mViewHolder(listItem);
//    }
//
//
//    /**
//     * Change recyclerview's item specified by position parameter
//     *
//     * @param holder    - view holder for recyclerview
//     * @param position  - list position of item to be changed
//     */
//    @Override
//    public void onBindViewHolder(@NonNull mViewHolder holder, int position) {
//        LinearLayout personalInfoArea = (LinearLayout) ((ViewGroup)holder.userInfoView).getChildAt(mViewHolder.INDEX_PERSONAL_INFO);
//
//        ImageView profilePicture = (ImageView) ((ViewGroup)holder.userInfoView).getChildAt(mViewHolder.INDEX_PROFILE_PICTURE);
//        TextView name = (TextView) personalInfoArea.getChildAt(mViewHolder.INDEX_NAME);
//        TextView serverAddress = (TextView) personalInfoArea.getChildAt(mViewHolder.INDEX_SERVER_ADDRESS);
//        TextView status = (TextView) ((ViewGroup)holder.userInfoView).getChildAt(mViewHolder.INDEX_STATUS);
//
//        profilePicture.setImageBitmap(dataset.get(position).profilePictureBitmap);
//        //profilePicture.setImageURI(Uri.parse(dataset.get(position).profilePicture));
//        name.setText(dataset.get(position).userName);
//        serverAddress.setText(dataset.get(position).streamAddress);
//        status.setText(dataset.get(position).userStatus);
//    }
//}




//public class PeersActivity extends AppCompatActivity {
//
//    // Essential elements for the recyclerview
//    RecyclerView peerListView;
//    RecyclerView.Adapter peerListAdapter;
//    LinearLayoutManager layoutManager;
//
//    ArrayList<LazyWebUser> peers_array;
//    LazyWebPeerGroups peers;
//
//    final int initialArraySize = 10;
//
//    public void refreshPeerList() {
//    }
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_peer_list);
//
//        /**
//         *  Setting Recycler View containing the list of information on the user's peers.
//         */
//        peerListView = findViewById(R.id.peerList);
//        peerListView.setHasFixedSize(true);
//
//        layoutManager = new LinearLayoutManager(this);
//        peerListView.setLayoutManager(layoutManager);
//
//        //UserInterfaceHandler.initButtonsUI(this);
//    }
//
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        try {
//            Thread peer_thread = new Thread() {
//                @Override
//                public void run() {
//                    try {
//                        peers = MyApplication.authHandler.getPeers();
//                    } catch (IOException | JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            };
//            peer_thread.start();
//            peer_thread.join();
//
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            finish();
//        }
//
//        peers_array = new ArrayList<LazyWebUser>(this.initialArraySize);
//        LazyWebUser[] protectees = peers.getProtectees();
//        LazyWebUser[] guardians = peers.getGuardians();
//        LazyWebUser[] requests_protectees = peers.getProtecteeRequests();
//        LazyWebUser[] requests_guardians = peers.getGuardianRequests();
//
//        peers_array.addAll(Arrays.asList(requests_protectees));
//        peers_array.addAll(Arrays.asList(requests_guardians));
//        peers_array.addAll(Arrays.asList(protectees));
//        peers_array.addAll(Arrays.asList(guardians));
//
//        peerListAdapter = new RecyclerViewAdapter(peers_array);
//        peerListView.setAdapter(peerListAdapter);
//
//        /*
//        for (int i = 0; i < guardians.length; i++) {
//
//        }
//        */
//    }
//
//
//    @Override
//    protected void onResume() {
//
//        super.onResume();
//    }
//
//
//    @Override
//    protected void onRestart() {
//
//        super.onRestart();
//    }
//
//
//    @Override
//    protected void onPause() {
//
//        super.onPause();
//    }
//
//    @Override
//    protected void onDestroy() {
//
//        super.onDestroy();
//    }
//}
