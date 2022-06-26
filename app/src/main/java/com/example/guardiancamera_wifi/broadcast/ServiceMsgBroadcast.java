package com.example.guardiancamera_wifi.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.guardiancamera_wifi.broadcast.headers.CameraMessages;
import com.example.guardiancamera_wifi.domain.model.EmergencyMessages;


abstract public class ServiceMsgBroadcast extends BroadcastReceiver {

    public ServiceMsgBroadcast() {
    }

    abstract public void onStreamStart();

    abstract public void onStreamStop();

    abstract public void onEmergencyStart(Intent intent);

    abstract public void onEmergencyStop();

    abstract public void onCameraConnected();

    abstract public void onCameraDisconnected();

    abstract public void onTempUpdate(int temp);

    abstract public void onBatteryUpdate(int remaining);

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        assert action != null;
        switch (action) {
            case EmergencyMessages.STREAM_READY:
                onStreamStart();
                break;

            case EmergencyMessages.CAMERA_CONNECTED:
                onCameraConnected();
                break;

            case EmergencyMessages.CAMERA_DISCONNECTED:
                onCameraDisconnected();
                break;

            case EmergencyMessages.EMERGENCY_STARTED:
                onEmergencyStart(intent);
                break;

            case EmergencyMessages.EMERGENCY_STOPPED:
                onEmergencyStop();
                break;

            case EmergencyMessages.STREAM_STOPPED:
                onStreamStop();
                break;

            case CameraMessages.CAMERA_BATTERY_STATE:
                assert extras != null;
                onBatteryUpdate(extras.getInt("remaining_battery"));
                break;

            case CameraMessages.CAMERA_TEMP:
                assert extras != null;
                onTempUpdate(extras.getInt("temp"));
                break;

            default:
                break;
        }
    }
}