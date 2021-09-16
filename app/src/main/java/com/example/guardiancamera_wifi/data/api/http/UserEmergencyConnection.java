package com.example.guardiancamera_wifi.data.api.http;

import android.content.Context;

import com.example.guardiancamera_wifi.Env;
import com.example.guardiancamera_wifi.MyApplication;
import com.example.guardiancamera_wifi.data.api.http.base.HttpConnection;
import com.example.guardiancamera_wifi.data.api.http.exceptions.RequestDeniedException;
import com.example.guardiancamera_wifi.data.configs.StreamingURI;
import com.example.guardiancamera_wifi.domain.models.HttpResponse;
import com.example.guardiancamera_wifi.data.configs.VideoConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;


public class UserEmergencyConnection extends HttpConnection {
    private Context appContext;

    public UserEmergencyConnection(final Context applicationContext) {
        this.appContext = applicationContext;
    }

    public JSONObject startStream(VideoConfig videoConfig) throws IOException, JSONException, RequestDeniedException {
        JSONObject streamInfo = new JSONObject();
        streamInfo.put("format", videoConfig.format);
        streamInfo.put("resolution", videoConfig.resolution);
        streamInfo.put("webToken", MyApplication.currentUser.webToken);
        String url = URI.PREFIX_HTTP + Env.STREAMING_SERVER_IP + StreamingURI.URI_STREAM + '/'
                + MyApplication.currentUser.uid;
        HttpResponse response = sendHttpRequest(url, streamInfo, HttpConnection.POST);
        JSONObject responseBody = new JSONObject(Arrays.toString(response.getBody()));
        if (responseBody.getBoolean("result")) {
            MyApplication.clientStreamInfo.setId(responseBody.getInt("id"));
            return responseBody;
        }
        else {
            throw new RequestDeniedException();
        }
    }


    public JSONObject stopStream() throws IOException, JSONException, RequestDeniedException {
        JSONObject data = new JSONObject();
        data.put("webToken", MyApplication.currentUser.webToken);
        String url = URI.PREFIX_HTTP + Env.STREAMING_SERVER_IP + StreamingURI.URI_STREAM + '/'
                + MyApplication.clientStreamInfo.getId();
        HttpResponse response = sendHttpRequest(url, new JSONObject(), HttpConnection.DELETE);
        JSONObject responseBody = new JSONObject(Arrays.toString(response.getBody()));
        if (responseBody.getBoolean("result"))
            return responseBody;
        else
            throw new RequestDeniedException();
    }


    public JSONObject startEmergency() throws IOException, JSONException, RequestDeniedException {
        JSONObject sendData = new JSONObject();
        sendData.put("webToken", MyApplication.currentUser.webToken);
        String url = URI.PREFIX_HTTP + Env.STREAMING_SERVER_IP + StreamingURI.URI_EMERGENCY + '/'
                + MyApplication.clientStreamInfo.getId();
        HttpResponse response = sendHttpRequest(url, sendData, HttpConnection.POST);
        JSONObject responseBody = new JSONObject(Arrays.toString(response.getBody()));
        if (responseBody.getBoolean("result"))
            return responseBody;
        else
            throw new RequestDeniedException();
    }


    public JSONObject stopEmergency() throws IOException, JSONException, RequestDeniedException {
        JSONObject sendData = new JSONObject();
        sendData.put("token", MyApplication.currentUser.webToken);
        String url = URI.PREFIX_HTTP + Env.STREAMING_SERVER_IP + StreamingURI.URI_EMERGENCY
                + MyApplication.clientStreamInfo.getId();
        HttpResponse response = sendHttpRequest(url, sendData, HttpConnection.DELETE);
        JSONObject responseBody = new JSONObject(Arrays.toString(response.getBody()));
        if (responseBody.getBoolean("result"))
            return responseBody;
        else
            throw new RequestDeniedException();
    }
}
