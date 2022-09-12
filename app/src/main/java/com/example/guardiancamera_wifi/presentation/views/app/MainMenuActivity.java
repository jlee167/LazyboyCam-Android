package com.example.guardiancamera_wifi.presentation.views.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.guardiancamera_wifi.MyApplication;
import com.example.guardiancamera_wifi.R;
import com.example.guardiancamera_wifi.broadcast.EmergencyBroadcast;
import com.example.guardiancamera_wifi.presentation.views.app.home.HomeFragment;
import com.example.guardiancamera_wifi.presentation.views.app.peerList.PeerListFragment;
import com.example.guardiancamera_wifi.presentation.views.app.setting.SettingsFragment;
import com.example.guardiancamera_wifi.presentation.views.app.watch.WatchStreamFragment;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;


class FragmentSelectButton {
    public ImageView icon;
    public TextView label;

    FragmentSelectButton(ImageView icon, TextView label) {
        this.icon = icon;
        this.label = label;
    }

    public ImageView getIcon() {
        return icon;
    }

    public TextView getLabel() {
        return label;
    }
}


public class MainMenuActivity extends AppCompatActivity {

    MainMenuPresenter presenter;

    ImageView viewVideoIcon;
    ImageView peerListIcon;
    ImageView settingIcon;
    ImageView homeIcon;
    TextView viewVideoLabel;
    TextView peerListLabel;
    TextView settingLabel;
    TextView homeLabel;

    Fragment homeFragment;
    Fragment watchStreamFragment;
    Fragment peerListFragment;
    Fragment settingsFragment;

    HashMap<Fragment, FragmentSelectButton> viewToBtnMapper;

    private void changeFragment(Fragment newFragment) {
        FrameLayout container = findViewById(R.id.contentsFrame);
        //container.removeAllViews();
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contentsFrame, newFragment);
        transaction.commit();

        for (Map.Entry<Fragment, FragmentSelectButton> elem : viewToBtnMapper.entrySet()) {
            if (elem.getKey() == newFragment) {
                elem.getValue().getIcon().setColorFilter(Color.rgb(0,170,250));
                elem.getValue().getLabel().setTextColor(Color.rgb(0,170,250));
            } else {
                elem.getValue().getIcon().setColorFilter(Color.WHITE);
                elem.getValue().getLabel().setTextColor(Color.WHITE);
            }
        }
    }


    /**
     * Create activity intents and initialize control buttons' UI.
     * Connect respective buttons to corresponding services and activities.
     */
    private void initViewObjects() {
        viewVideoIcon = findViewById(R.id.videoViewBtn);
        viewVideoLabel = findViewById(R.id.videoViewLabel);

        peerListIcon = findViewById(R.id.peersViewBtn);
        peerListLabel = findViewById(R.id.peersViewLabel);

        settingIcon = findViewById(R.id.settingsViewBtn);
        settingLabel = findViewById(R.id.settingsViewLabel);

        homeIcon = findViewById(R.id.homeViewBtn);
        homeLabel = findViewById(R.id.homeViewLabel);

        homeFragment = new HomeFragment();
        watchStreamFragment = new WatchStreamFragment();
        peerListFragment = new PeerListFragment();
        settingsFragment = new SettingsFragment();

        viewVideoIcon.setOnClickListener(v -> changeFragment(watchStreamFragment));
        viewVideoLabel.setOnClickListener(v -> changeFragment(watchStreamFragment));
        peerListIcon.setOnClickListener(v -> changeFragment(peerListFragment));
        peerListLabel.setOnClickListener(v -> changeFragment(peerListFragment));
        settingIcon.setOnClickListener(v -> changeFragment(settingsFragment));
        settingLabel.setOnClickListener(v -> changeFragment(settingsFragment));
        homeIcon.setOnClickListener(v -> changeFragment(homeFragment));
        homeLabel.setOnClickListener(v -> changeFragment(homeFragment));

        ImageView profilePicture = findViewById(R.id.userProfileImage);
        Picasso.get().load(MyApplication.currentUser.getProfileImageUrl()).into(profilePicture);
    }


    private void mapButtonsToFragments() {
        viewToBtnMapper = new HashMap<>();
        viewToBtnMapper.put(homeFragment, new FragmentSelectButton(homeIcon, homeLabel));
        viewToBtnMapper.put(peerListFragment, new FragmentSelectButton(peerListIcon, peerListLabel));
        viewToBtnMapper.put(watchStreamFragment, new FragmentSelectButton(viewVideoIcon, viewVideoLabel));
        viewToBtnMapper.put(settingsFragment, new FragmentSelectButton(settingIcon, settingLabel));
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

        initViewObjects();
        mapButtonsToFragments();

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

        changeFragment(homeFragment);
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
