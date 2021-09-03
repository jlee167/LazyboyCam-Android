package com.example.guardiancamera_wifi.domain.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.guardiancamera_wifi.domain.models.EmergencyMessages;


abstract public class ServiceMsgBroadcast extends BroadcastReceiver {

    public ServiceMsgBroadcast() {
    }

    abstract public void onStreamStart();

    abstract public void onStreamStop();

    abstract public void onEmergencyStart(Intent intent);

    abstract public void onEmergencyStop();

    abstract public void onCameraConnected();

    abstract public void onCameraDisconnected();

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        assert action != null;
        switch (action) {
            case EmergencyMessages.START_STREAM:
                onStreamStart();
                break;

            case EmergencyMessages.CAMERA_CONNECTED:
                onCameraConnected();
                break;

            case EmergencyMessages.CAMERA_DISCONNECTED:
                onCameraDisconnected();
                break;

            case EmergencyMessages.START_EMERGENCY:
                onEmergencyStart(intent);
                break;

            case EmergencyMessages.STOP_EMERGENCY:
                onEmergencyStop();
                break;

            case EmergencyMessages.STOP_STREAM:
                onStreamStop();
                break;

            default:
                break;
        }
    }
}