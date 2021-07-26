package com.example.guardiancamera_wifi.networking.http;

import android.content.Context;


public class EmergencyServerConnection extends HttpConnection {

    public String jwt;
    public String streamID;

    public EmergencyServerConnection(String jwt, final Context applicationContext) {
        this.jwt = jwt;
        appContext = applicationContext;
    }

    /*
    public void jwtGen() throws IOException, JSONException {
        sendHttpRequest(MyApplication.currentUser.username, new JSONObject(), httpConnection.POST);
    }

    public void startStream() throws JSONException, IOException {
        JSONObject sendPacket = new JSONObject();
        sendPacket.put("webToken", Jwt);
        sendHttpRequest(MyApplication.currentUser.username, sendPacket, httpConnection.POST);
    }

    public void stopStream() throws JSONException, IOException {
        JSONObject sendPacket = new JSONObject();
        sendPacket.put("webToken", Jwt);
        sendPacket.put("", MyApplication.currentUser);
        sendHttpRequest(this.streamID, sendPacket, httpConnection.DELETE);
    }

    public void startEmergency() throws JSONException, IOException {
        JSONObject sendPacket = new JSONObject();
        sendPacket.put("webToken", Jwt);
        sendPacket.put("", MyApplication.currentUser);
        sendHttpRequest(this.streamID, sendPacket, httpConnection.POST);
    }

    public void stopEmergency() throws JSONException, IOException {
        JSONObject sendPacket = new JSONObject();
        sendPacket.put("webToken", Jwt);
        sendPacket.put("", MyApplication.currentUser);
        sendHttpRequest(this.streamID, sendPacket, httpConnection.DELETE);
    }
    */
}
