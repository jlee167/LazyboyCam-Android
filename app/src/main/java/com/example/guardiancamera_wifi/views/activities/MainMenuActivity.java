package com.example.guardiancamera_wifi.views.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.guardiancamera_wifi.R;
import com.example.guardiancamera_wifi.broadcastReceivers.EmergencyBroadcast;
import com.example.guardiancamera_wifi.configs.VideoConfig;
import com.example.guardiancamera_wifi.services.EmergencyService;
import com.example.guardiancamera_wifi.services.GeolocationService;
import com.example.guardiancamera_wifi.views.fragments.HomeFragment;
import com.example.guardiancamera_wifi.views.fragments.PeerListFragment;
import com.example.guardiancamera_wifi.views.fragments.SettingsFragment;
import com.example.guardiancamera_wifi.views.fragments.StreamFragment;


public class MainMenuActivity extends AppCompatActivity {

    /* Control Buttons */
    TextView captureServiceBtn;
    TextView viewVideoBtn;
    TextView peerListBtn;
    TextView settingBtn;
    TextView homeBtn;

    Intent emergencyIntent;
    Intent geoLocationIntent;


    /**
     * Helper function for changing fragment
     *
     * @param newFragment Target Fragment
     */
    private void changeFragment(Fragment newFragment) {
        FrameLayout container = (FrameLayout) findViewById(R.id.contentsFrame);
        container.removeAllViews();

        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.contentsFrame, newFragment);
        transaction.commit();
    }


    /**
     * Create activity intents and initialize control buttons' UI.
     * Connect respective buttons to corresponding services and activities.
     */
    private void initButtonsUI() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("MainMenuActivity");
        BroadcastReceiver serviceMsgReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Bundle extras = intent.getExtras();

                assert action != null;
                switch (action) {
                    case "stream.start":
                        ((TextView)findViewById(R.id.streamStatus)).setText("Active");
                        ((TextView)findViewById(R.id.streamLog)).append("Stream Started. \n");
                        ((TextView)findViewById(R.id.streamLog)).append(
                                "Video URL: " + intent.getStringExtra("videoDestUrl") + "\n");
                        ((TextView)findViewById(R.id.streamLog)).append(
                                "Audio URL: " + intent.getStringExtra("audioDestUrl") +"\n");
                        ((TextView)findViewById(R.id.streamLog)).append(
                                "Geo URL: " + intent.getStringExtra("geoDestUrl") +"\n");
                        break;

                    case "camera.connected":
                        ((TextView)findViewById(R.id.cameraStatus)).setText("Online");
                        ((TextView)findViewById(R.id.streamLog)).append(
                                "Camera Connected. Serial: " + VideoConfig.serialNumber +"\n");
                        break;

                    case "camera.disconnected":
                        ((TextView)findViewById(R.id.cameraStatus)).setText("Offline");
                        ((TextView)findViewById(R.id.streamLog)).append(
                                "Camera Disconnected" + VideoConfig.serialNumber +"\n");
                        break;

                    case "emergency.start":
                        geoLocationIntent.putExtra("destUrl",
                                intent.getStringExtra("geoDestUrl"));

                        if (GeolocationService.isRunning()) {
                            stopService(geoLocationIntent);
                            while (GeolocationService.isRunning()) {};
                        }
                        startService(geoLocationIntent);

                        ((TextView)findViewById(R.id.clientStatus)).setText("Danger");
                        ((TextView)findViewById(R.id.streamLog)).append(
                                "Emergency Protocol Started. \n");
                        break;

                    case "emergency.stop":
                        if (GeolocationService.isRunning())
                            stopService(geoLocationIntent);

                        ((TextView)findViewById(R.id.clientStatus)).setText("Fine");
                        ((TextView)findViewById(R.id.streamLog)).append(
                                "Emergency is over. \n");
                        break;

                    case "stream.stop":
                        ((TextView)findViewById(R.id.streamStatus)).setText("Inctive");
                        ((TextView)findViewById(R.id.streamLog)).append("Stream Stopped. \n");

                    default:
                        break;
                }
            }
        };
        registerReceiver(serviceMsgReceiver, filter);

        captureServiceBtn = findViewById(R.id.captureStartBtn);
        captureServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!EmergencyService.isRunning()) {
                    startService(emergencyIntent);
                    captureServiceBtn.setText(R.string.MENU_STOP_CAPTURE);
                } else {
                    stopService(emergencyIntent);
                    captureServiceBtn.setText(R.string.MENU_START_CAPTURE);
                }
            }
        });


        viewVideoBtn = (TextView) findViewById(R.id.videoViewBtn);
        viewVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(new StreamFragment());
            }
        });


        peerListBtn = (TextView) findViewById(R.id.peerListBtn);
        peerListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(new PeerListFragment());
            }
        });

        settingBtn = (TextView) findViewById(R.id.settingBtn);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(new SettingsFragment());
            }
        });


        homeBtn = (TextView) findViewById(R.id.homeBtn);
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment homeFragment = new HomeFragment();
                changeFragment(homeFragment);
            }
        });
    }

    private void getPermission() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS);
        String [] permissionList = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.RECORD_AUDIO
        };

        if (permission == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS},
                    100);
        }
        ActivityCompat.requestPermissions(this, permissionList,200);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        emergencyIntent = new Intent(this, EmergencyService.class);
        geoLocationIntent = new Intent(this, GeolocationService.class);;

        this.getPermission();

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

        // Initialize Control Buttons
        initButtonsUI();
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

        if (EmergencyService.isRunning())
            stopService(new Intent(this, EmergencyService.class));
        if (GeolocationService.isRunning())
            stopService(new Intent(this, GeolocationService.class));
    }
}
