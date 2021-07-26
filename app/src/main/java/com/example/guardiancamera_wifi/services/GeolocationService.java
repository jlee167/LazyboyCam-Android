package com.example.guardiancamera_wifi.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.guardiancamera_wifi.configs.EmergencyStream;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class GeolocationService extends Service {

    // Running status of the service. True when one or more instance is running.
    // There should not be more than one instance running concurrently!
    private static boolean runState;

    private StreamHandler serviceController;
    private Looper serviceLooper;

    private Location location;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private URL targetUrl;
    private HttpURLConnection conn;
    private BufferedOutputStream outputStream;
    private BufferedInputStream inputStream;

    private HandlerThread handlerThread;

    /**
     * @return True if the service is running.
     */
    public static boolean isRunning() {
        return runState;
    }


    /**
     * Default Constructor
     * Do nothing.
     */
    public GeolocationService() throws MalformedURLException {
        this.targetUrl = new URL(EmergencyStream.getGeoDestUrl());
    }


    /**
     * Message handler for this service.
     */
    private final class StreamHandler extends Handler {

        public StreamHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();

        try {
            conn = (HttpURLConnection) targetUrl.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
            return;
        }
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        conn.setDoInput(true);

        try {
            conn.connect();
            outputStream = new BufferedOutputStream(conn.getOutputStream());
            inputStream = new BufferedInputStream(conn.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                JSONObject locationData = new JSONObject();
                try {
                    locationData.put("latitude", location.getLatitude());
                    locationData.put("longitude", location.getLongitude());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    outputStream.write(locationData.toString().getBytes());
                    outputStream.flush();
                    outputStream.close();
                    int responseCode = conn.getResponseCode();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ///ActivityCompat.requestPermissions(this.getActivity, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            ///ActivityCompat.requestPermissions(this, [Manifest.permission.ACCESS_FINE_LOCATION], 200);
            ///ActivityCompat.requestPermissions(this, [Manifest.permission.ACCESS_FINE_LOCATION], 300);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, locationListener);

        handlerThread = new HandlerThread("Geodata Service Controller", Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        serviceLooper = handlerThread.getLooper();
        serviceController = new StreamHandler(serviceLooper);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Message msg = serviceController.obtainMessage();
        msg.arg1 = startId;
        serviceController.sendMessage(msg);

        runState = true;
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // @TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        runState = false;
        handlerThread.quitSafely();
        super.onDestroy();
    }
}
