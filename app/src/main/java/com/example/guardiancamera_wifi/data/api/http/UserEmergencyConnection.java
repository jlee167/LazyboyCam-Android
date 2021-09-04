package com.example.guardiancamera_wifi.data.api.http;

import android.content.Context;

import com.example.guardiancamera_wifi.MyApplication;
import com.example.guardiancamera_wifi.data.api.http.base.HttpConnection;
import com.example.guardiancamera_wifi.data.api.http.exceptions.RequestDeniedException;
import com.example.guardiancamera_wifi.data.configs.IpTable;
import com.example.guardiancamera_wifi.data.configs.StreamingURI;
import com.example.guardiancamera_wifi.domain.models.HttpResponse;
import com.example.guardiancamera_wifi.domain.models.VideoConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;


public class UserEmergencyConnection extends HttpConnection {

    private String webToken;
    private String streamID;

    public UserEmergencyConnection(final Context applicationContext, String webToken) {
        appContext = applicationContext;
        this.webToken = webToken;
    }

    private JSONObject startStream() throws IOException, JSONException, RequestDeniedException {
        JSONObject streamInfo = new JSONObject();
        streamInfo.put("format", VideoConfig.format);
        streamInfo.put("resolution", VideoConfig.resolution);
        streamInfo.put("token", MyApplication.currentUser.webToken);
        String url = IpTable.PREFIX_HTTP + IpTable.STREAMING_SERVER_IP + StreamingURI.URI_STREAM + '/'
                + MyApplication.currentUser.username;
        HttpResponse response = sendHttpRequest(url, streamInfo, HttpConnection.POST);
        JSONObject responseBody = new JSONObject(Arrays.toString(response.getBody()));
        if (responseBody.getBoolean("result")) {
            this.streamID = responseBody.getString("id");
            return responseBody;
        }
        else {
            throw new RequestDeniedException();
        }
    }


    private JSONObject stopStream() throws IOException, JSONException, RequestDeniedException {
        JSONObject data = new JSONObject();
        data.put("token", MyApplication.currentUser.webToken);
        String url = IpTable.PREFIX_HTTP + IpTable.STREAMING_SERVER_IP + StreamingURI.URI_STREAM;
        HttpResponse response = sendHttpRequest(url, new JSONObject(), HttpConnection.DELETE);
        JSONObject responseBody = new JSONObject(Arrays.toString(response.getBody()));
        if (responseBody.getBoolean("result"))
            return responseBody;
        else
            throw new RequestDeniedException();
    }


    private JSONObject startEmergency() throws IOException, JSONException {
        JSONObject sendData = new JSONObject();
        sendData.put("token", MyApplication.currentUser.webToken);
        String url = IpTable.PREFIX_HTTP + IpTable.STREAMING_SERVER_IP + StreamingURI.URI_EMERGENCY;
        HttpResponse result = sendHttpRequest(url, sendData, HttpConnection.POST);
        return new JSONObject(Arrays.toString(result.getBody()));
    }


    private JSONObject stopEmergency() throws IOException, JSONException {
        JSONObject sendData = new JSONObject();
        sendData.put("token", MyApplication.currentUser.webToken);
        String url = IpTable.PREFIX_HTTP + IpTable.STREAMING_SERVER_IP + StreamingURI.URI_EMERGENCY;
        HttpResponse result = sendHttpRequest(url, sendData, HttpConnection.DELETE);
        return new JSONObject(Arrays.toString(result.getBody()));
    }
}
