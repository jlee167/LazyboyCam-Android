package com.example.guardiancamera_wifi.presentation.views.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.guardiancamera_wifi.R;
import com.example.guardiancamera_wifi.domain.broadcasts.EmergencyBroadcast;
import com.example.guardiancamera_wifi.domain.models.ClientStreamData;
import com.example.guardiancamera_wifi.domain.models.VideoConfig;
import com.example.guardiancamera_wifi.presentation.views.app.home.HomeFragment;
import com.example.guardiancamera_wifi.presentation.views.app.peerList.PeerListFragment;
import com.example.guardiancamera_wifi.presentation.views.app.setting.SettingsFragment;
import com.example.guardiancamera_wifi.presentation.views.app.watch.WatchStreamFragment;


public class MainMenuActivity extends AppCompatActivity {

    MainMenuPresenter presenter;

    TextView captureServiceBtn;
    TextView viewVideoBtn;
    TextView peerListBtn;
    TextView settingBtn;
    TextView homeBtn;


    private void changeFragment(Fragment newFragment) {
        FrameLayout container = findViewById(R.id.contentsFrame);
        container.removeAllViews();
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.contentsFrame, newFragment);
        transaction.commit();
    }

    public void onStreamStart(ClientStreamData clientStreamData) {
        ((TextView) findViewById(R.id.streamStatusView)).setText("Active");
        ((TextView) findViewById(R.id.systemLog)).append("Stream Started. \n");
        ((TextView) findViewById(R.id.systemLog)).append(
                "Video URL: " + clientStreamData.getVideoDestUrl() + "\n");
        ((TextView) findViewById(R.id.systemLog)).append(
                "Audio URL: " + clientStreamData.getAudioDestUrl() + "\n");
        ((TextView) findViewById(R.id.systemLog)).append(
                "Geo URL: " + clientStreamData.getGeoDestUrl() + "\n");
    }

    public void onCameraConnected() {
        ((TextView) findViewById(R.id.cameraStatusView)).setText("Online");
        ((TextView) findViewById(R.id.systemLog)).append(
                "Camera Connected. Serial: " + VideoConfig.serialNumber + "\n");
    }

    public void onCameraDisconnected() {
        ((TextView) findViewById(R.id.cameraStatusView)).setText("Offline");
        ((TextView) findViewById(R.id.systemLog)).append(
                "Camera Disconnected" + VideoConfig.serialNumber + "\n");
    }

    public void onEmergencyStart(ClientStreamData clientStreamData) {
        ((TextView) findViewById(R.id.userStatusView)).setText("Danger");
        ((TextView) findViewById(R.id.systemLog)).append(
                "Emergency Protocol Started. \n");
    }

    public void onEmergencyStop() {
        ((TextView) findViewById(R.id.userStatusView)).setText("Fine");
        ((TextView) findViewById(R.id.systemLog)).append(
                "Emergency is over. \n");
    }

    public void onStreamStop() {
        ((TextView) findViewById(R.id.streamStatusView)).setText("Inctive");
        ((TextView) findViewById(R.id.systemLog)).append("Stream Stopped. \n");
    }

    /**
     * Create activity intents and initialize control buttons' UI.
     * Connect respective buttons to corresponding services and activities.
     */
    private void initUI() {
        captureServiceBtn = findViewById(R.id.captureStartBtn);
        viewVideoBtn = findViewById(R.id.videoViewBtn);
        peerListBtn = findViewById(R.id.peerListBtn);
        settingBtn = findViewById(R.id.settingBtn);
        homeBtn = findViewById(R.id.homeBtn);

        captureServiceBtn.setOnClickListener(v -> {
            if (!presenter.isStreaming()) {
                presenter.startStreamingService();
                captureServiceBtn.setText(R.string.MENU_STOP_CAPTURE);
            } else {
                presenter.stopStreamingService();
                captureServiceBtn.setText(R.string.MENU_START_CAPTURE);
            }
        });

        viewVideoBtn.setOnClickListener(v -> changeFragment(new WatchStreamFragment()));
        peerListBtn.setOnClickListener(v -> changeFragment(new PeerListFragment()));
        settingBtn.setOnClickListener(v -> changeFragment(new SettingsFragment()));
        homeBtn.setOnClickListener(v -> {
            Fragment homeFragment = new HomeFragment();
            changeFragment(homeFragment);
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);


        presenter = new MainMenuPresenter(getApplicationContext(), this);
        presenter.getPermission();
        presenter.startServiceBroadcastReceiver();
        presenter.startSMSReceiver();

        initUI();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Lazyboy Notification Channel";
            String description = "Emergency Notification Channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("LazyBoyChannel", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        /*
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "LazyBoyChannel")
                .setSmallIcon(R.drawable.kakaoaccount_icon)
                .setContentTitle("Emergency Notification")
                .setContentText("Emergency Text")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, builder.build());
        */

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        EmergencyBroadcast emergencyBroadcast = new EmergencyBroadcast();
        registerReceiver(emergencyBroadcast, filter);

    }


    @Override
    protected void onStart() {
        super.onStart();

    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}
