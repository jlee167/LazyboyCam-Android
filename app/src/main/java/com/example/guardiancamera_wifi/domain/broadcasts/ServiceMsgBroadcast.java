package com.example.guardiancamera_wifi.domain.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


abstract public class ServiceMsgBroadcast extends BroadcastReceiver {

    public ServiceMsgBroadcast() {
    }

    abstract public void onStreamStart();

    abstract public void onStreamStop();

    abstract public void onEmergencyStart();

    abstract public void onEmergencyStop();

    abstract public void onCameraConnected();

    abstract public void onCameraDisconnected();

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        assert action != null;
        switch (action) {
            case "stream.start":
                onStreamStart();
                break;

            case "camera.connected":
                onCameraConnected();
                break;

            case "camera.disconnected":
                onCameraDisconnected();
                break;

            case "emergency.start":
                onEmergencyStart();
                break;

            case "emergency.stop":
                onEmergencyStop();
                break;

            case "stream.stop":
                onStreamStop();
                break;

            default:
                break;
        }
    }
}