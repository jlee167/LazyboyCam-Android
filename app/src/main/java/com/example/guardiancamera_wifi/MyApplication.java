package com.example.guardiancamera_wifi;

import android.app.Application;
import android.content.Context;
import androidx.lifecycle.MutableLiveData;
import com.example.guardiancamera_wifi.domain.models.VideoConfig;
import com.example.guardiancamera_wifi.domain.models.LazyWebPeers;
import com.example.guardiancamera_wifi.domain.models.LazyWebUser;
import com.example.guardiancamera_wifi.data.api.http.MainServerConnection;
import com.example.guardiancamera_wifi.data.api.http.EmergencyServerConnection;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.ConcurrentLinkedDeque;




public class MyApplication extends Application {

    /* Current User */
    public static LazyWebUser currentUser;

    /* Peers (Guardians & Protecteds) of current user */
    public static LazyWebPeers peers;

    /* HTTP handler for Authentication & Web server */
    public static MainServerConnection mainServerConn;

    /* HTTP handler for Emergency Streaming server */
    public static EmergencyServerConnection emergencyServerConnection;

    // Application & Peripheral Configuration
    public static VideoConfig videoConfig;

    public static ConcurrentLinkedDeque<String> appLogs;

    public static void setCurrentUser(LazyWebUser userinfo) {
        currentUser = userinfo;
    }

    public static MutableLiveData<ConcurrentLinkedDeque<String>> applicationLogLiveData =
            new MutableLiveData<ConcurrentLinkedDeque<String>>();


    public static void applicationLog(String msg) {
        if (appLogs.size() >= 50)
            appLogs.removeFirst();

        appLogs.add("[" + Calendar.getInstance().getTime().toString().split("GMT")[0] + "]" +msg);
        applicationLogLiveData.setValue(appLogs);
    }

    public static void postApplicationLog(String msg) {
        if (appLogs.size() >= 50)
            appLogs.removeFirst();

        appLogs.add("[" + Calendar.getInstance().getTime().toString().split("GMT")[0] + "]" +msg);
        applicationLogLiveData.postValue(appLogs);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        videoConfig = new VideoConfig(this);
        appLogs = new ConcurrentLinkedDeque<String>();

        try {
            mainServerConn = new MainServerConnection(getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // SDK Initialization
        KakaoSDK.init(new KakaoAdapter() {

            @Override
            public IApplicationConfig getApplicationConfig() {
                return new IApplicationConfig() {
                    @Override
                    public Context getApplicationContext() {
                        return MyApplication.this;
                    }
                };
            }
        });
    }
}
