package com.example.guardiancamera_wifi;

import android.app.Application;
import android.content.Context;
import androidx.lifecycle.MutableLiveData;

import com.example.guardiancamera_wifi.domain.model.Stream;
import com.example.guardiancamera_wifi.domain.model.Peers;
import com.example.guardiancamera_wifi.domain.model.User;
import com.example.guardiancamera_wifi.data.net.http.MainServer;
import com.example.guardiancamera_wifi.data.net.http.StreamingServer;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;

import java.util.Calendar;
import java.util.concurrent.ConcurrentLinkedDeque;


public class MyApplication extends Application {

    public static User currentUser;
    public static Peers peers;
    public static MainServer mainServerConn;
    public static StreamingServer streamingServer;
    public static Stream clientStream;

    public static ConcurrentLinkedDeque<String> appLogs;

    public static void setCurrentUser(User user) {
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

        mainServerConn = new MainServer();
        streamingServer = new StreamingServer();
        clientStream = new Stream();

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
