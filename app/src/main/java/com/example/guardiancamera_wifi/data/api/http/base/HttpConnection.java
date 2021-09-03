package com.example.guardiancamera_wifi.data.api.http.base;

import android.content.Context;
import android.util.Log;

import com.example.guardiancamera_wifi.data.configs.Addresses;
import com.example.guardiancamera_wifi.domain.models.HttpResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpConnection {
    public static String GET    = "GET";
    public static String POST   = "POST";
    public static String PUT    = "PUT";
    public static String DELETE = "DELETE";

    /* Caller activity context & passed intent */
    protected Context appContext;

    /**
     * @param sendData
     * @param method
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public HttpResponse sendHttpRequest(String url, JSONObject sendData, String method)
            throws IOException, JSONException {
        BufferedOutputStream outputStream;
        BufferedInputStream inputStream;
        URL authServerUrl;
        HttpURLConnection conn;

        authServerUrl = new URL(Addresses.PREFIX_HTTP + url);

        boolean outputEnabled = method.equals(POST) || method.equals(PUT);

        try {
            conn = (HttpURLConnection) authServerUrl.openConnection();
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod(method);

            if (outputEnabled)
                conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();

            if (outputEnabled) {
                try {
                    /* Http output stream */
                    outputStream = new BufferedOutputStream(conn.getOutputStream());
                    outputStream.write(sendData.toString().getBytes());
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            int responseCode = conn.getResponseCode();
            inputStream = new BufferedInputStream(conn.getInputStream());
            byte[] responseBody = new byte[1000];
            inputStream.read(responseBody);

            return new HttpResponse(responseCode, responseBody);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
