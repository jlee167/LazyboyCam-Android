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
import com.example.guardiancamera_wifi.broadcast.EmergencyBroadcast;
import com.example.guardiancamera_wifi.presentation.views.app.home.HomeFragment;
import com.example.guardiancamera_wifi.presentation.views.app.peerList.PeerListFragment;
import com.example.guardiancamera_wifi.presentation.views.app.setting.SettingsFragment;
import com.example.guardiancamera_wifi.presentation.views.app.watch.WatchStreamFragment;


public class MainMenuActivity extends AppCompatActivity {

    MainMenuPresenter presenter;

    TextView viewVideoBtn;
    TextView peerListBtn;
    TextView settingBtn;
    TextView homeBtn;

    Fragment homeFragment;
    Fragment watchStreamFragment;
    Fragment peerListFragment;
    Fragment settingsFragment;

    private void changeFragment(Fragment newFragment) {
        FrameLayout container = findViewById(R.id.contentsFrame);
        //container.removeAllViews();
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contentsFrame, newFragment);
        transaction.commit();
    }


    /**
     * Create activity intents and initialize control buttons' UI.
     * Connect respective buttons to corresponding services and activities.
     */
    private void initUI() {
        viewVideoBtn = findViewById(R.id.videoViewBtn);
        peerListBtn = findViewById(R.id.peerListBtn);
        settingBtn = findViewById(R.id.settingBtn);
        homeBtn = findViewById(R.id.homeBtn);

        homeFragment = new HomeFragment();
        watchStreamFragment = new WatchStreamFragment();
        peerListFragment = new PeerListFragment();
        settingsFragment = new SettingsFragment();

        viewVideoBtn.setOnClickListener(v -> changeFragment(watchStreamFragment));
        peerListBtn.setOnClickListener(v -> changeFragment(peerListFragment));
        settingBtn.setOnClickListener(v -> changeFragment(settingsFragment));
        homeBtn.setOnClickListener(v -> changeFragment(homeFragment));
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.LightText);
        setContentView(R.layout.activity_main_menu);


        presenter = new MainMenuPresenter(getApplicationContext(), this);
        presenter.getPermission();
        presenter.startServiceMessageReceiver();
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
