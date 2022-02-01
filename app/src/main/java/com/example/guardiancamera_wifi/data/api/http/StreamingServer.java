package com.example.guardiancamera_wifi.data.api.http;

import android.content.Context;

import com.example.guardiancamera_wifi.Env;
import com.example.guardiancamera_wifi.MyApplication;
import com.example.guardiancamera_wifi.data.utils.HttpConnection;
import com.example.guardiancamera_wifi.data.exceptions.RequestDeniedException;
import com.example.guardiancamera_wifi.data.utils.StreamingURI;
import com.example.guardiancamera_wifi.domain.model.HttpResponse;
import com.example.guardiancamera_wifi.data.utils.VideoConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class StreamingServer extends HttpConnection {
    private Context appContext;

    public StreamingServer(final Context applicationContext) {
        this.appContext = applicationContext;
    }

    public JSONObject startStream(VideoConfig videoConfig) throws IOException, JSONException, RequestDeniedException {
        JSONObject body = new JSONObject();
        body.put("protocol", videoConfig.format);
        body.put("resolution", videoConfig.resolution);

        JSONObject header = new JSONObject();
        header.put("webToken", MyApplication.currentUser.getStreamAccessToken());

        String url = Env.STREAMING_SERVER_IP + StreamingURI.URI_STREAM + '/'
                + MyApplication.currentUser.getUid();
        HttpResponse response = sendHttpRequest(url, header, body, HttpConnection.POST);
        JSONObject responseBody = new JSONObject(new String(response.getBody()));
        if (responseBody.getBoolean("result")) {
            MyApplication.clientStream.setId(responseBody.getInt("id"));
            return responseBody;
        }
        else {
            throw new RequestDeniedException();
        }
    }


    public JSONObject stopStream() throws IOException, JSONException, RequestDeniedException {
        JSONObject body = new JSONObject();
        body.put("webToken", MyApplication.currentUser.getPrivateKey());

        JSONObject header = new JSONObject();
        header.put("webToken", MyApplication.currentUser.getPrivateKey());

        String url = Env.STREAMING_SERVER_IP + StreamingURI.URI_STREAM + '/'
                + MyApplication.clientStream.getId();
        HttpResponse response = sendHttpRequest(url, header, new JSONObject(), HttpConnection.DELETE);
        JSONObject responseBody = new JSONObject(new String(response.getBody()));
        if (responseBody.getBoolean("result"))
            return responseBody;
        else
            throw new RequestDeniedException();
    }


    public JSONObject startEmergency() throws IOException, JSONException, RequestDeniedException {
        JSONObject sendData = new JSONObject();
        sendData.put("webToken", MyApplication.currentUser.getPrivateKey());

        JSONObject header = new JSONObject();
        header.put("webToken", MyApplication.currentUser.getPrivateKey());

        String url = Env.STREAMING_SERVER_IP + StreamingURI.URI_EMERGENCY + '/'
                + MyApplication.clientStream.getId();
        HttpResponse response = sendHttpRequest(url, header, sendData, HttpConnection.POST);
        JSONObject responseBody = new JSONObject(new String(response.getBody()));
        if (responseBody.getBoolean("result"))
            return responseBody;
        else
            throw new RequestDeniedException();
    }


    public JSONObject stopEmergency() throws IOException, JSONException, RequestDeniedException {
        JSONObject sendData = new JSONObject();
        sendData.put("token", MyApplication.currentUser.getPrivateKey());

        JSONObject header = new JSONObject();
        header.put("webToken", MyApplication.currentUser.getPrivateKey());


        String url = Env.STREAMING_SERVER_IP + StreamingURI.URI_EMERGENCY
                + MyApplication.clientStream.getId();
        HttpResponse response = sendHttpRequest(url, header, sendData, HttpConnection.DELETE);
        JSONObject responseBody = new JSONObject(new String(response.getBody()));
        if (responseBody.getBoolean("result"))
            return responseBody;
        else
            throw new RequestDeniedException();
    }
}
