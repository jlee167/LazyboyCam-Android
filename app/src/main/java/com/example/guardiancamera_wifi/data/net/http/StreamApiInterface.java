package com.example.guardiancamera_wifi.data.net.http;

import com.example.guardiancamera_wifi.data.exceptions.RequestDeniedException;
import com.example.guardiancamera_wifi.data.utils.VideoConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public interface StreamApiInterface {

    JSONObject startStream(VideoConfig videoConfig) throws IOException, JSONException, RequestDeniedException;
    JSONObject stopStream() throws IOException, JSONException, RequestDeniedException;
    JSONObject startEmergency() throws IOException, JSONException, RequestDeniedException;
    JSONObject stopEmergency() throws IOException, JSONException, RequestDeniedException;
    void sendLocation(JSONObject location) throws JSONException, IOException, RequestDeniedException;
}
