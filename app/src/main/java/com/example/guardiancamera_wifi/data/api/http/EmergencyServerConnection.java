package com.example.guardiancamera_wifi.data.api.http;

import android.content.Context;

import com.example.guardiancamera_wifi.data.api.http.base.HttpConnection;


public class EmergencyServerConnection extends HttpConnection {

    public String jwt;
    public String streamID;

    public EmergencyServerConnection(final Context applicationContext) {
        appContext = applicationContext;
    }
}
