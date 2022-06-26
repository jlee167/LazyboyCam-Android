package com.example.guardiancamera_wifi.presentation.views.app.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.example.guardiancamera_wifi.broadcast.ServiceMsgBroadcast;
import com.example.guardiancamera_wifi.broadcast.headers.CameraMessages;
import com.example.guardiancamera_wifi.domain.model.EmergencyMessages;
import com.example.guardiancamera_wifi.service.EmergencyService;
import com.example.guardiancamera_wifi.service.exceptions.InEmergencyException;
import com.example.guardiancamera_wifi.service.exceptions.TimeoutException;

public class HomePresenter {

    Context applicationContext;
    HomeFragment fragment;
    Intent emergencyIntent;

    BroadcastReceiver serviceMsgReceiver;


    HomePresenter(Context context, HomeFragment fragment) {
        this.fragment = fragment;
        this.applicationContext = context;
        this.emergencyIntent = new Intent(fragment.getActivity(), EmergencyService.class);
    }

    public void handleServiceBtnClick() throws InEmergencyException, TimeoutException {
        if (!EmergencyService.isServiceRunning()) {
            applicationContext.startService(emergencyIntent);
        } else {
            if (!EmergencyService.isEmergency())
                applicationContext.stopService(emergencyIntent);
            else
                throw new InEmergencyException();
        }
    }

    public void startServiceMessageReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(EmergencyMessages.STREAM_READY);
        filter.addAction(EmergencyMessages.STREAM_STOPPED);
        filter.addAction(EmergencyMessages.CAMERA_CONNECTED);
        filter.addAction(EmergencyMessages.CAMERA_DISCONNECTED);
        filter.addAction(EmergencyMessages.EMERGENCY_STARTED);
        filter.addAction(EmergencyMessages.EMERGENCY_STOPPED);
        filter.addAction(CameraMessages.CAMERA_BATTERY_STATE);
        filter.addAction(CameraMessages.CAMERA_TEMP);

        serviceMsgReceiver = new ServiceMsgBroadcast() {
            @Override
            public void onStreamStart() {
                fragment.onStreamStart();
            }

            @Override
            public void onStreamStop() {
                fragment.onStreamStop();
            }

            @Override
            public void onEmergencyStart(Intent intent) {
                fragment.onEmergencyStart();
            }

            @Override
            public void onEmergencyStop() {
                fragment.onEmergencyStop();
            }

            @Override
            public void onCameraConnected() {
                fragment.onCameraConnected();
            }

            @Override
            public void onCameraDisconnected() {
                fragment.onCameraDisconnected();
            }

            @Override
            public void onBatteryUpdate(int remaining) {
                fragment.onBatteryUpdate(remaining);
            }

            @Override
            public void onTempUpdate(int temp) {
                fragment.onTempUpdate(temp);
            }
        };
        fragment.requireActivity().registerReceiver(serviceMsgReceiver, filter);
    }

    public void stopServiceMessageReceiver() {
        fragment.requireActivity().unregisterReceiver(serviceMsgReceiver);
    }
}
