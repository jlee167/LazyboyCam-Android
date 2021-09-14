package com.example.guardiancamera_wifi;

import android.app.Application;
import android.content.Context;
import androidx.lifecycle.MutableLiveData;

import com.example.guardiancamera_wifi.domain.models.ClientStreamInfo;
import com.example.guardiancamera_wifi.data.configs.VideoConfig;
import com.example.guardiancamera_wifi.domain.models.LazyWebPeers;
import com.example.guardiancamera_wifi.domain.models.LazyWebUser;
import com.example.guardiancamera_wifi.data.api.http.MainServerConnection;
import com.example.guardiancamera_wifi.data.api.http.UserEmergencyConnection;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;

import java.util.Calendar;
import java.util.concurrent.ConcurrentLinkedDeque;


public class MyApplication extends Application {

    public static LazyWebUser currentUser;
    public static LazyWebPeers peers;
    public static MainServerConnection mainServerConn;
    public static UserEmergencyConnection userEmergencyConnection;
    public static ClientStreamInfo clientStreamInfo;

    public static ConcurrentLinkedDeque<String> appLogs;

    public static void setCurrentUser(LazyWebUser user) {
        /* @Todo: Add Exception Handler for case when a user is already logged in */
        currentUser = user;
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

        appLogs = new ConcurrentLinkedDeque<String>();

        mainServerConn = new MainServerConnection();

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
