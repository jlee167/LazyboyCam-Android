package com.example.guardiancamera_wifi.presentation.views.app.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.guardiancamera_wifi.Env;
import com.example.guardiancamera_wifi.MyApplication;
import com.example.guardiancamera_wifi.R;
import com.example.guardiancamera_wifi.domain.service.EmergencyService;
import com.example.guardiancamera_wifi.domain.service.exceptions.InEmergencyException;
import com.example.guardiancamera_wifi.domain.service.exceptions.TimeoutException;
import com.squareup.picasso.Picasso;

import java.io.InputStream;


public class HomeFragment extends Fragment {

    Activity activity;
    HomePresenter presenter;

    TextView streamingServiceBtn;
    TextView userStatusView;
    TextView cameraStatusView;
    TextView streamStatusView;

    int defaultTextColor;


    public void updateStreamingServiceUI() {
        if (EmergencyService.isServiceRunning()) {
            streamingServiceBtn.setText(R.string.MENU_STOP_CAPTURE);
        } else {
            streamingServiceBtn.setText(R.string.MENU_START_CAPTURE);
        }
    }

    public void initUI() {
        streamingServiceBtn = activity.findViewById(R.id.captureStartBtn);
        userStatusView = activity.findViewById(R.id.userStatusView);
        cameraStatusView = activity.findViewById(R.id.cameraStatusView);
        streamStatusView = activity.findViewById(R.id.streamStatusView);
        defaultTextColor = userStatusView.getCurrentTextColor();

        streamingServiceBtn.setText(R.string.MENU_START_CAPTURE);

        this.onCameraDisconnected();
        this.onStreamStop();
        this.onEmergencyStop();

        streamingServiceBtn.setOnClickListener(v -> {
            try {
                presenter.handleServiceBtnClick();
                updateStreamingServiceUI();
            } catch (InEmergencyException | TimeoutException e) {
                //@Todo: Toast Error message
                e.printStackTrace();
            }
        });

        AssetManager assetManager = getResources().getAssets();
        InputStream imgInputStream;
        try {
            ImageView serverImage = activity.findViewById(R.id.serverImage);
            imgInputStream = assetManager.open("flat-icons/icons8-server-48.png");
            serverImage.setImageBitmap(BitmapFactory.decodeStream(imgInputStream));
            imgInputStream.close();

            ImageView guardianImage = activity.findViewById(R.id.guardianImage);
            imgInputStream = assetManager.open("flat-icons/icons8-operator-58.png");
            guardianImage.setImageBitmap(BitmapFactory.decodeStream(imgInputStream));
            imgInputStream.close();

            ImageView protectedImage = activity.findViewById(R.id.protectedImage);
            imgInputStream = assetManager.open("flat-icons/icons8-shield-64.png");
            protectedImage.setImageBitmap(BitmapFactory.decodeStream(imgInputStream));
            imgInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public HomeFragment() {
    }


    public void onStreamStart() {
        streamStatusView.setText(R.string.STREAM_STATUS_ACTIVE);
        streamStatusView.setTextColor(Color.rgb(0, 151, 136));
        updateStreamingServiceUI();
    }


    public void onStreamStop() {
        streamStatusView.setText(R.string.STREAM_STATUS_INACTIVE);
        streamStatusView.setTextColor(this.defaultTextColor);
        updateStreamingServiceUI();
    }


    public void onEmergencyStart() {
        userStatusView.setText(R.string.USER_STATUS_EMERGENCY);
        userStatusView.setTextColor(Color.RED);
    }


    public void onEmergencyStop() {
        userStatusView.setText(R.string.USER_STATUS_FINE);
        userStatusView.setTextColor(Color.rgb(0, 151, 136));
    }


    public void onCameraConnected() {
        cameraStatusView.setText(R.string.CAMERA_STATUS_CONNECTED);
        cameraStatusView.setTextColor(Color.rgb(0, 151, 136));
    }


    public void onCameraDisconnected() {
        cameraStatusView.setText(R.string.CAMERA_STATUS_DISCONNECTED);
        cameraStatusView.setTextColor(this.defaultTextColor);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onResume() {
        super.onResume();

        ImageView profileImage = activity.findViewById(R.id.userProfileImage);
        Picasso.get().load(MyApplication.currentUser.getProfileImageUrl()).into(profileImage);
        TextView username = activity.findViewById(R.id.username);
        username.setText(MyApplication.currentUser.getUsername());

        TextView serverAddressView = activity.findViewById(R.id.serverAddressView);
        serverAddressView.setText(Env.STREAMING_SERVER_IP);

        TextView protectedsCountView = activity.findViewById(R.id.protectedsCountView);
        String protectedsCount = Integer.toString(MyApplication.peers.getProtecteds().length);
        protectedsCountView.setText("You have " + protectedsCount + " protecteds");

        TextView guardiansCountView = activity.findViewById(R.id.guardiansCountView);
        String guardiansCount = Integer.toString(MyApplication.peers.getGuardians().length);
        guardiansCountView.setText("You have " + guardiansCount + " guardians");


        onCameraDisconnected();
        onStreamStop();
        onEmergencyStop();


        if (EmergencyService.isCamConnected()) {
            onCameraConnected();
        }

        if (EmergencyService.isStreamRunning()) {
            onStreamStart();
        }

        if (EmergencyService.isEmergency()) {
            onEmergencyStart();
        }

        presenter.startServiceMessageReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.stopServiceMessageReceiver();
    }

    @Override
    public void onStart() {
        super.onStart();
        activity = getActivity();
        assert activity != null;
        presenter = new HomePresenter(getActivity().getApplicationContext(), this);
        initUI();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
