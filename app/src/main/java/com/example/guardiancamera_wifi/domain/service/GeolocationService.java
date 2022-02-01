package com.example.guardiancamera_wifi.domain.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import androidx.core.app.ActivityCompat;

import com.example.guardiancamera_wifi.data.api.http.base.HttpConnection;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class GeolocationService extends Service {

    private static boolean runState;

    private StreamHandler serviceController;
    private Looper serviceLooper;

    private Location location;
    private LocationManager locationManager;
    public LocationListener locationListener;
    private HandlerThread handlerThread;

    private String geoDestUrl;
    private String webToken;
    private HttpConnection conn;


    /**
     * @return True if the service is running.
     */
    public static boolean isRunning() {
        return runState;
    }


    /**
     * Message handler for this service.
     */
    private final class StreamHandler extends Handler {

        public StreamHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NotNull Message msg) {
            {
                /* @Todo Error Notification */
                if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    return;
                if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    return;
            }

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            conn = new HttpConnection();
            locationListener = location -> {
                try {
                    JSONObject header = new JSONObject();
                    header.put("webToken", webToken);
                    JSONObject body = new JSONObject();
                    body.put("latitude", location.getLatitude());
                    body.put("longitude", location.getLongitude());
                    body.put("timestamp", System.currentTimeMillis());
                    conn.sendHttpRequest(geoDestUrl, header, body, HttpConnection.POST);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    2000, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    2000, 0, locationListener);
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (runState) {
            /* @Todo: error message to toast */
            return START_NOT_STICKY;
        }
        else
            runState = true;

        geoDestUrl = intent.getStringExtra("geoDestUrl");
        webToken = intent.getStringExtra("webToken");
        handlerThread = new HandlerThread("Geodata Service Controller", Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        serviceLooper = handlerThread.getLooper();
        serviceController = new StreamHandler(serviceLooper);
        serviceController.sendEmptyMessage(0);
        return START_NOT_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // @TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onDestroy() {
        runState = false;
        locationManager.removeUpdates(locationListener);
        serviceLooper.quitSafely();
        handlerThread.quitSafely();
        super.onDestroy();
    }
}
