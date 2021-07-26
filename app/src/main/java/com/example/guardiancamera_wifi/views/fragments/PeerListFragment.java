package com.example.guardiancamera_wifi.views.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.guardiancamera_wifi.models.LazyWebPeers;
import com.example.guardiancamera_wifi.R;
import com.example.guardiancamera_wifi.models.LazyWebUser;
import com.example.guardiancamera_wifi.models.MyApplication;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutionException;


class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.mViewHolder> {

    // Dataset Array containing peer info objects
    ArrayList<LazyWebUser> dataset;


    public RecyclerViewAdapter() {
        dataset = new ArrayList<LazyWebUser>();
    }


    public RecyclerViewAdapter(ArrayList<LazyWebUser> inputDataset) {
        dataset = new ArrayList<LazyWebUser>();
        dataset.addAll(inputDataset);
    }


    public void addItem(LazyWebUser item) {
        dataset.add(item);
    }


    @Override
    public int getItemCount() {
        return dataset.size();
    }


    /**
     * View Holder format for recyclerview
     * Consists of profile picture, personal information, and streaming address
     */
    static class mViewHolder extends RecyclerView.ViewHolder {

        // Child indexes of views within corresponding layouts.
        static int INDEX_PROFILE_PICTURE = 0;
        static int INDEX_PERSONAL_INFO = 1;
        static int INDEX_NAME = 0;
        static int INDEX_SERVER_ADDRESS = 1;

        View userInfoView;

        mViewHolder(@NonNull View itemView) {
            super(itemView);
            userInfoView = itemView;
        }
    }


    /**
     * Generate a view holder for recyclerview
     *
     * @param parent   -   View that will be used for each list
     * @param viewType -
     * @return Viewholder with a view item generated from 'recycleritem' layout
     */
    @NonNull
    @Override
    public mViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleritem, parent, false);
        return new mViewHolder(listItem);
    }


    /**
     * Change recyclerview's item specified by position parameter
     *
     * @param holder   - view holder for recyclerview
     * @param position - list position of item to be changed
     */
    @Override
    public void onBindViewHolder(@NonNull mViewHolder holder, int position) {
        LinearLayout personalInfoArea = (LinearLayout) ((ViewGroup) holder.userInfoView).getChildAt(mViewHolder.INDEX_PERSONAL_INFO);

        ImageView profilePicture = (ImageView) ((ViewGroup) holder.userInfoView).getChildAt(mViewHolder.INDEX_PROFILE_PICTURE);

        TextView name = (TextView) personalInfoArea.getChildAt(mViewHolder.INDEX_NAME);
        TextView serverAddress = (TextView) personalInfoArea.getChildAt(mViewHolder.INDEX_SERVER_ADDRESS);


        profilePicture.setImageBitmap(dataset.get(position).profileImage);
        name.setText(dataset.get(position).username);
        serverAddress.setText(dataset.get(position).streamAddress);
    }
}


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PeerListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PeerListFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    /* Essential elements for the recyclerview */
    RecyclerView peerListView;
    RecyclerViewAdapter guardianListAdapter, protectedListAdapter;
    LinearLayoutManager layoutManager;

    ArrayList<LazyWebUser> peersArray;
    private LazyWebPeers peers;


    public void refreshPeerList() {
        try {
            MyApplication.peers = MyApplication.mainServerConn.getPeers();
        } catch (JSONException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        this.peers = MyApplication.peers;

        LazyWebUser[] protecteds = this.peers.getProtecteds();
        LazyWebUser[] guardians = this.peers.getGuardians();

        ArrayList<LazyWebUser> guardianArrayList = new ArrayList<LazyWebUser>(guardians.length);
        ArrayList<LazyWebUser> protectedArrayList = new ArrayList<LazyWebUser>(protecteds.length);

        guardianArrayList.addAll(Arrays.asList(guardians));
        protectedArrayList.addAll(Arrays.asList(protecteds));

        for (LazyWebUser user : guardianArrayList)
            guardianListAdapter.addItem(user);
        for (LazyWebUser user : protectedArrayList)
            protectedListAdapter.addItem(user);

        peerListView.setAdapter(guardianListAdapter);
    }


    public PeerListFragment() {
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PeerListFragment newInstance(String param1, String param2) {
        PeerListFragment fragment = new PeerListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        guardianListAdapter = new RecyclerViewAdapter();
        protectedListAdapter = new RecyclerViewAdapter();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_peers, container, false);
    }

    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        peerListView = requireActivity().findViewById(R.id.peerList);
        peerListView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        peerListView.setLayoutManager(layoutManager);

        refreshPeerList();

        TabLayout tabLayout = requireActivity().findViewById(R.id.peerGroupTab);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        peerListView.setAdapter(guardianListAdapter);
                        break;

                    case 1:
                        peerListView.setAdapter(protectedListAdapter);
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }
}