package com.example.guardiancamera_wifi.services;

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

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.guardiancamera_wifi.configs.Addresses;
import com.example.guardiancamera_wifi.configs.EmergencyStream;
import com.example.guardiancamera_wifi.networking.http.HttpConnection;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class GeolocationService extends Service {

    // Running status of the service. True when one or more instance is running.
    // There should not be more than one instance running concurrently!
    private static boolean runState;

    private StreamHandler serviceController;
    private Looper serviceLooper;

    private Location location;
    private LocationManager locationManager;
    public LocationListener locationListener;
    private HandlerThread handlerThread;

    /* Streaming Server Objects */
    HttpConnection conn;


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
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    try {
                        JSONObject locationData = new JSONObject();
                        locationData.put("latitude", location.getLatitude());
                        locationData.put("longitude", location.getLongitude());
                        locationData.put("timestamp", System.currentTimeMillis());

                        conn.sendHttpRequest(
                                EmergencyStream.getGeoDestUrl(),
                                locationData,
                                HttpConnection.POST,
                                Addresses.STREAMING_SERVER_IP
                        );
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
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
        if (runState)
            return super.onStartCommand(intent, flags, startId);
        else
            runState = true;

        handlerThread = new HandlerThread("Geodata Service Controller", Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        serviceLooper = handlerThread.getLooper();
        serviceController = new StreamHandler(serviceLooper);
        serviceController.sendEmptyMessage(0);

        return super.onStartCommand(intent, flags, startId);
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
