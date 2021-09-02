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
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.guardiancamera_wifi.MyApplication;
import com.example.guardiancamera_wifi.domain.broadcasts.EmergencyBroadcast;
import com.example.guardiancamera_wifi.domain.models.EmergencyMessages;
import com.example.guardiancamera_wifi.domain.services.EmergencyService;
import com.example.guardiancamera_wifi.domain.services.GeolocationService;

public class MainMenuPresenter {

    MainMenuActivity activity;
    Context applicationContext;
    Intent emergencyIntent;
    Intent geoLocationIntent;

    BroadcastReceiver serviceMsgReceiver;
    EmergencyBroadcast emergencyBroadcast;

    MainMenuPresenter(Context context, MainMenuActivity activity) {
        this.activity = activity;
        this.applicationContext = context;
        this.emergencyIntent = new Intent(activity, EmergencyService.class);
        this.geoLocationIntent = new Intent(activity, GeolocationService.class);
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
        if (EmergencyService.isRunning())
            activity.stopService(new Intent(activity, EmergencyService.class));
        if (GeolocationService.isRunning())
            activity.stopService(new Intent(activity, GeolocationService.class));
    }

    public boolean isStreaming() {
        return EmergencyService.isRunning();
    }

    public void startStreamingService() {
        activity.startService(emergencyIntent);
    }

    public void stopStreamingService() {
        activity.stopService(emergencyIntent);
    }

    public void stopGeoLocationService() {
        if (GeolocationService.isRunning())
            activity.stopService(geoLocationIntent);
    }

    public void startGeoLocationService(String geoDestUrl) {
        geoLocationIntent.putExtra("destUrl", geoDestUrl);

        if (GeolocationService.isRunning())
            activity.stopService(geoLocationIntent);
        activity.startService(geoLocationIntent);
    }


    public void startServiceBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("MainMenuActivity");
        serviceMsgReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Bundle extras = intent.getExtras();

                assert action != null;
                switch (action) {
                    case EmergencyMessages.START_STREAM:
                        activity.onStreamStart(MyApplication.clientStreamData);
                        break;

                    case EmergencyMessages.CAMERA_CONNECTED:
                        activity.onCameraConnected();
                        break;

                    case EmergencyMessages.CAMERA_DISCONNECTED:
                        activity.onCameraDisconnected();
                        break;

                    case EmergencyMessages.START_EMERGENCY:
                        startGeoLocationService(intent.getStringExtra("geoDestUrl"));
                        activity.onEmergencyStart(MyApplication.clientStreamData);
                        break;

                    case EmergencyMessages.STOP_EMERGENCY:
                        stopGeoLocationService();
                        activity.onEmergencyStop();
                        break;

                    case EmergencyMessages.STOP_STREAM:
                        activity.onStreamStop();
                        break;

                    default:
                        break;
                }
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
        emergencyBroadcast = new EmergencyBroadcast();
        activity.registerReceiver(emergencyBroadcast, filter);
    }
}
