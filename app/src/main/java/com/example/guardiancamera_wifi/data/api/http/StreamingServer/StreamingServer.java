package com.example.guardiancamera_wifi.data.api.http.StreamingServer;

import android.util.Log;

import com.example.guardiancamera_wifi.Env;
import com.example.guardiancamera_wifi.MyApplication;
import com.example.guardiancamera_wifi.data.exceptions.RequestDeniedException;
import com.example.guardiancamera_wifi.data.api.http.HttpConnection;
import com.example.guardiancamera_wifi.data.utils.StreamingURI;
import com.example.guardiancamera_wifi.data.utils.VideoConfig;
import com.example.guardiancamera_wifi.domain.model.HttpResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;


public class StreamingServer extends HttpConnection implements StreamApiInterface {

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
        body.put("webToken", MyApplication.currentUser.getStreamAccessToken());

        JSONObject header = new JSONObject();
        header.put("webToken", MyApplication.currentUser.getStreamAccessToken());

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


    public void sendJpeg(byte[] data, String urlString) throws IOException {
        BufferedOutputStream outputStream;
        BufferedInputStream inputStream;
        URL url;
        HttpURLConnection httpConn;

        url = new URL(urlString);
        httpConn = (HttpURLConnection) url.openConnection();

        httpConn.setRequestProperty("Content-Type", "application/json");//""application/octet-stream");
        httpConn.setRequestMethod(HttpConnection.POST);

        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        httpConn.connect();

        outputStream = new BufferedOutputStream(httpConn.getOutputStream());
        outputStream.write(data);
        outputStream.flush();
        /*
        int offset = 0;
        while(image.length > offset) {
            int len;
            if ((image.length - offset) > 1000)
                len = 1000;
            else
                len = image.length - offset;
            outputStream.write(image, offset, len);
            outputStream.flush();
            offset = offset + len;
        }
        */
        outputStream.close();

        if (httpConn.getResponseCode() != 200) {
            inputStream = new BufferedInputStream(httpConn.getInputStream());
            byte[] responseBody = new byte[1000];
            inputStream.read(responseBody);
            Log.i("", Arrays.toString(responseBody));
        }
    }


    public void sendLocation(JSONObject location) throws JSONException, IOException, RequestDeniedException {
        JSONObject header = new JSONObject();
        header.put("webToken", MyApplication.currentUser.getStreamAccessToken());
        String url = Env.STREAMING_SERVER_IP + StreamingURI.URI_STREAM + '/'
                + MyApplication.clientStream.getId() + "/geo";
        HttpResponse response = sendHttpRequest(url, header, location, HttpConnection.POST);
        if (response.getCode() != 200)
            throw new RequestDeniedException();
    }
}
