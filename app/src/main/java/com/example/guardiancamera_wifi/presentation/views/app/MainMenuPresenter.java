package com.example.guardiancamera_wifi.presentation.views.app;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.guardiancamera_wifi.broadcast.EmergencyBroadcast;
import com.example.guardiancamera_wifi.broadcast.ServiceMsgBroadcast;
import com.example.guardiancamera_wifi.service.EmergencyService;

public class MainMenuPresenter {

    MainMenuActivity activity;
    Context applicationContext;
    Intent emergencyIntent;

    BroadcastReceiver serviceMsgReceiver;
    EmergencyBroadcast emergencyBroadcast;

    MainMenuPresenter(Context context, MainMenuActivity activity) {
        this.activity = activity;
        this.applicationContext = context;
        this.emergencyIntent = new Intent(activity, EmergencyService.class);
    }

    public void getPermission() {
        int permission = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECEIVE_SMS);
        String[] permissionList = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.INTERNET,
                Manifest.permission.RECORD_AUDIO
        };

        if (permission == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECEIVE_SMS},
                    100);
        }
        ActivityCompat.requestPermissions(activity, permissionList, 200);
    }

    public void onDestroy() {
        if (EmergencyService.isServiceRunning())
            activity.stopService(new Intent(activity, EmergencyService.class));
    }


    public void startServiceMessageReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("MainMenuActivity");

        serviceMsgReceiver = new ServiceMsgBroadcast() {
            @Override
            public void onStreamStart() {
            }

            @Override
            public void onStreamStop() {
            }

            @Override
            public void onEmergencyStart(Intent intent) {
            }

            @Override
            public void onEmergencyStop() {

            }

            @Override
            public void onCameraConnected() {
            }

            @Override
            public void onCameraDisconnected() {
            }
        };
        activity.registerReceiver(serviceMsgReceiver, filter);
    }

    public void startSMSReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Lazyboy Notification Channel";
            String description = "Emergency Notification Channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("LazyBoyChannel", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = activity.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        emergencyBroadcast = new EmergencyBroadcast();
        activity.registerReceiver(emergencyBroadcast, filter);
    }
}
