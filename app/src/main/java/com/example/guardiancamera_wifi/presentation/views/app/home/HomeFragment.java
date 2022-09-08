package com.example.guardiancamera_wifi.presentation.views.app.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.guardiancamera_wifi.MyApplication;
import com.example.guardiancamera_wifi.R;
import com.example.guardiancamera_wifi.service.EmergencyService;
import com.example.guardiancamera_wifi.service.exceptions.InEmergencyException;
import com.example.guardiancamera_wifi.service.exceptions.TimeoutException;
import com.squareup.picasso.Picasso;


public class HomeFragment extends Fragment {

    Activity activity;
    HomePresenter presenter;

    ConstraintLayout streamingServiceBtn;
    TextView streamingServiceText;
    TextView camBtnText;


    TextView userStatusView;
    TextView cameraStatusView;
    TextView streamStatusView;

    CardView userStatusCard;
    CardView cameraStatusCard;
    CardView streamStatusCard;

    TextView batteryMeterText;
    TextView tempMeterText;
    ProgressBar batteryMeterBar;
    ProgressBar tempMeterBar;

    int defaultTextColor;
    int blackTextColor;


    public void updateStreamingServiceUI() {
        if (EmergencyService.isServiceRunning()) {
            streamingServiceText.setText(R.string.MENU_STOP_CAPTURE);
        } else {
            streamingServiceText.setText(R.string.MENU_START_CAPTURE);
        }
    }


    public void updateTempMeter(int temp) {
        tempMeterText.setText(temp + "%");
        tempMeterBar.setIndeterminate(false);
        tempMeterBar.setProgress(temp);
    }


    public void updateBatteryMeter(int remaining){
        batteryMeterText.setText(remaining + "%");
        batteryMeterBar.setIndeterminate(false);
        batteryMeterBar.setProgress(remaining);
    }


    public void initUI() {

        defaultTextColor = Color.rgb(255, 0,0);

        streamingServiceBtn = activity.findViewById(R.id.captureStartBtn);
        streamingServiceText = activity.findViewById(R.id.captureBtnText);
        camBtnText = activity.findViewById(R.id.camBtnText);

        userStatusView = activity.findViewById(R.id.userStatusView);
        userStatusCard = activity.findViewById(R.id.userStatusCard);
        cameraStatusView = activity.findViewById(R.id.cameraStatusView);
        cameraStatusCard = activity.findViewById(R.id.cameraStatusCard);
        streamStatusView = activity.findViewById(R.id.streamStatusView);
        streamStatusCard = activity.findViewById(R.id.streamStatusCard);

        batteryMeterBar = activity.findViewById(R.id.batteryMeter);
        tempMeterBar = activity.findViewById(R.id.tempMeter);
        batteryMeterText = activity.findViewById(R.id.batteryMeterText);
        tempMeterText = activity.findViewById(R.id.tempMeterText);
        batteryMeterText.setTextColor(Color.rgb(0x00, 0xa8, 0x2b));
        tempMeterText.setTextColor(Color.rgb(0xff, 0x48, 0x48));

        userStatusView.setTextColor(defaultTextColor);
        cameraStatusView.setTextColor(defaultTextColor);
        streamStatusView.setTextColor(defaultTextColor);

        streamingServiceText.setText(R.string.MENU_START_CAPTURE);
        camBtnText.setText(R.string.MENU_CONNECT_CAMERA);
        camBtnText.setTextColor(Color.rgb(255, 255,255));

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

        /*AssetManager assetManager = getResources().getAssets();
        InputStream imgInputStream;
        try {
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
        }*/

        updateTempMeter(25);
        updateBatteryMeter(100);

        batteryMeterText.setText(R.string.METER_LEVEL_DISCONNECTED);
        batteryMeterBar.setIndeterminate(false);
        batteryMeterBar.setProgress(0);
        tempMeterText.setText(R.string.METER_LEVEL_DISCONNECTED);
        tempMeterBar.setIndeterminate(false);
        tempMeterBar.setProgress(0);
    }


    public HomeFragment() {
    }


    public void onStreamStart() {
        streamStatusView.setText(R.string.STREAM_STATUS_ACTIVE);
        streamStatusView.setTextColor(Color.rgb(0, 255, 0));
        streamStatusCard.setCardBackgroundColor(Color.argb(100,0,255,0));
        updateStreamingServiceUI();
    }


    public void onStreamStop() {
        streamStatusView.setText(R.string.STREAM_STATUS_INACTIVE);
        streamStatusView.setTextColor(this.defaultTextColor);
        streamStatusCard.setCardBackgroundColor(Color.rgb(45,46,67));
        updateStreamingServiceUI();
    }


    public void onEmergencyStart() {
        userStatusView.setText(R.string.USER_STATUS_EMERGENCY);
        userStatusView.setTextColor(Color.RED);
    }


    public void onEmergencyStop() {
        userStatusView.setText(R.string.USER_STATUS_FINE);
        userStatusView.setTextColor(Color.rgb(0, 255, 0));
        userStatusCard.setCardBackgroundColor(Color.argb(100,0,255,0));
    }


    public void onCameraConnected() {
        cameraStatusView.setText(R.string.CAMERA_STATUS_CONNECTED);
        cameraStatusView.setTextColor(Color.rgb(0, 255, 0));
        cameraStatusCard.setCardBackgroundColor(Color.argb(100,0,255,0));
    }


    public void onCameraDisconnected() {
        cameraStatusView.setText(R.string.CAMERA_STATUS_DISCONNECTED);
        cameraStatusView.setTextColor(this.defaultTextColor);
        cameraStatusCard.setCardBackgroundColor(Color.rgb(45,46,67));
    }

    public void onTempUpdate(int temp) {
        tempMeterBar.setProgress(temp);
    }

    public void onBatteryUpdate(int remaining) {
        batteryMeterBar.setProgress(remaining);
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


        TextView protectedsCountView = activity.findViewById(R.id.protectedsCountView);
        String protectedsCount = Integer.toString(MyApplication.peers.getProtecteds().length);
        protectedsCountView.setText(protectedsCount);

        TextView guardiansCountView = activity.findViewById(R.id.guardiansCountView);
        String guardiansCount = Integer.toString(MyApplication.peers.getGuardians().length);
        guardiansCountView.setText(guardiansCount);


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
