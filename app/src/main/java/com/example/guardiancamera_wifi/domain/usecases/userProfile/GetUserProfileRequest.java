package com.example.guardiancamera_wifi.domain.usecases.userProfile;

import com.example.guardiancamera_wifi.data.api.http.MainServerConnection;

public class GetUserProfileRequest {

    private MainServerConnection mainServerConn;

    public MainServerConnection getMainServerConn() {
        return mainServerConn;
    }

    public void setMainServerConnection(MainServerConnection mainServerConn) {
        this.mainServerConn = mainServerConn;
    }
}
