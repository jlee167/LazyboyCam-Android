package com.example.guardiancamera_wifi.data.api.http.base;

import com.example.guardiancamera_wifi.data.configs.IpTable;
import com.example.guardiancamera_wifi.domain.models.HttpResponse;

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

    /**
     * @param sendData
     * @param method
     * @return
     * @throws IOException
     */
    public HttpResponse sendHttpRequest(String url, JSONObject sendData, String method)
            throws IOException {
        BufferedOutputStream outputStream;
        BufferedInputStream inputStream;
        URL authServerUrl;
        HttpURLConnection conn;

        authServerUrl = new URL(IpTable.PREFIX_HTTP + url);

        boolean outputEnabled = method.equals(POST) || method.equals(PUT);

        conn = (HttpURLConnection) authServerUrl.openConnection();
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestMethod(method);

        if (outputEnabled)
            conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.connect();

        if (outputEnabled) {
            outputStream = new BufferedOutputStream(conn.getOutputStream());
            outputStream.write(sendData.toString().getBytes());
            outputStream.flush();
            outputStream.close();
        }

        int responseCode = conn.getResponseCode();
        inputStream = new BufferedInputStream(conn.getInputStream());
        byte[] responseBody = new byte[1000];
        inputStream.read(responseBody);

        return new HttpResponse(responseCode, responseBody);
    }
}
