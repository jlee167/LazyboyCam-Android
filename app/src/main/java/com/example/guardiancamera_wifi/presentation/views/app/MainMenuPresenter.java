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

import com.example.guardiancamera_wifi.MyApplication;
import com.example.guardiancamera_wifi.domain.broadcasts.EmergencyBroadcast;
import com.example.guardiancamera_wifi.domain.broadcasts.ServiceMsgBroadcast;
import com.example.guardiancamera_wifi.domain.services.EmergencyService;
import com.example.guardiancamera_wifi.domain.services.GeolocationService;
import com.example.guardiancamera_wifi.domain.services.exceptions.InEmergencyException;
import com.example.guardiancamera_wifi.domain.services.exceptions.TimeoutException;

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

    public void handleServiceBtnClick() throws InEmergencyException, TimeoutException {
        if (!EmergencyService.isRunning()) {
            applicationContext.startService(emergencyIntent);

            int timeoutCnt = 0;
            while (!EmergencyService.isRunning()) {
                timeoutCnt++;
                if (timeoutCnt > 100000) { //@Todo: make a TIMEOUT_THRESHOLD variable in config dir
                    throw new TimeoutException();
                }
            };
        }
        else {
            if (!EmergencyService.isEmergency())
                applicationContext.stopService(emergencyIntent);
            else
                throw new InEmergencyException();
        }
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


    public void startServiceMessageReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("MainMenuActivity");

        serviceMsgReceiver = new ServiceMsgBroadcast() {
            @Override
            public void onStreamStart() {
                activity.onStreamStart(MyApplication.clientStreamInfo);
            }

            @Override
            public void onStreamStop() {
                activity.onStreamStop();
            }

            @Override
            public void onEmergencyStart(Intent intent) {
                startGeoLocationService(intent.getStringExtra("geoDestUrl"));
                activity.onEmergencyStart(MyApplication.clientStreamInfo);
            }

            @Override
            public void onEmergencyStop() {
                stopGeoLocationService();
                activity.onEmergencyStop();
            }

            @Override
            public void onCameraConnected() {
                activity.onCameraConnected();
            }

            @Override
            public void onCameraDisconnected() {
                activity.onCameraDisconnected();
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
