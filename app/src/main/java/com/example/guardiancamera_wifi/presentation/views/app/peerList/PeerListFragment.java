package com.example.guardiancamera_wifi.presentation.views.app.peerList;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guardiancamera_wifi.MyApplication;
import com.example.guardiancamera_wifi.R;
import com.example.guardiancamera_wifi.domain.model.Peers;
import com.example.guardiancamera_wifi.domain.model.User;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutionException;


class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.mViewHolder> {

    ArrayList<User> dataset;


    public RecyclerViewAdapter() {
        dataset = new ArrayList<User>();
    }


    public RecyclerViewAdapter(ArrayList<User> inputDataset) {
        dataset = new ArrayList<User>();
        dataset.addAll(inputDataset);
    }


    public void addItem(User item) {
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
        static int INDEX_PICTURE_LAYOUT = 0;
        static int INDEX_PICTURE_CONTAINER = 0;
        static int INDEX_PROFILE_PICTURE = 0;
        static int INDEX_PERSONAL_INFO = 1;
        static int INDEX_WATCH_BUTTON = 2;
        static int INDEX_NAME = 1;
        static int INDEX_USER_ID = 0;


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
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull mViewHolder holder, int position) {
        LinearLayout personalInfoArea = (LinearLayout) ((ViewGroup) holder.userInfoView).getChildAt(mViewHolder.INDEX_PERSONAL_INFO);

        ConstraintLayout profilePictureLayout = (ConstraintLayout) ((ViewGroup) holder.userInfoView).getChildAt(mViewHolder.INDEX_PICTURE_LAYOUT);
        CardView profilePictureContainer = (CardView) profilePictureLayout.getChildAt(mViewHolder.INDEX_PICTURE_CONTAINER);
        ImageView profilePicture = (ImageView)  profilePictureContainer.getChildAt(mViewHolder.INDEX_PROFILE_PICTURE);

        Button watchBtn = (Button) ((ViewGroup) holder.userInfoView).getChildAt(mViewHolder.INDEX_WATCH_BUTTON);

        TextView name = (TextView) personalInfoArea.getChildAt(mViewHolder.INDEX_NAME);
        TextView userID = (TextView) personalInfoArea.getChildAt(mViewHolder.INDEX_USER_ID);

        User user = dataset.get(position);
        if (user.getStreamAddress() == null || user.getStreamAddress().isEmpty() ) {
            watchBtn.setVisibility(View.INVISIBLE);
        }

        /*try {
            profilePicture.setImageURI(Uri.parse(user.getProfileImageUrl()));
        } catch(Exception e) {
            Log.e("Profile image", Objects.requireNonNull(e.getMessage()));
        }*/

        Picasso.get().load(dataset.get(position).getProfileImageUrl()).into(profilePicture);
        name.setText(dataset.get(position).getUsername());
        userID.setText("ID: " + dataset.get(position).getUid());
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

    ArrayList<User> peersArray;
    private Peers peers;


    public void refreshPeerList() {
        try {
            MyApplication.peers = MyApplication.mainServerConn.getPeers();
        } catch (JSONException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        this.peers = MyApplication.peers;

        User[] protecteds = this.peers.getProtecteds();
        User[] guardians = this.peers.getGuardians();

        ArrayList<User> guardianArrayList = new ArrayList<User>(guardians.length);
        ArrayList<User> protectedArrayList = new ArrayList<User>(protecteds.length);

        guardianArrayList.addAll(Arrays.asList(guardians));
        protectedArrayList.addAll(Arrays.asList(protecteds));

        for (User user : guardianArrayList)
            guardianListAdapter.addItem(user);
        for (User user : protectedArrayList)
            protectedListAdapter.addItem(user);

        peerListView.setAdapter(guardianListAdapter);
    }


    public PeerListFragment() {
    }


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