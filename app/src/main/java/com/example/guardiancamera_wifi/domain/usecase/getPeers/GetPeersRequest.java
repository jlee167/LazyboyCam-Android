package com.example.guardiancamera_wifi.domain.usecase.getPeers;

import com.example.guardiancamera_wifi.data.api.http.MainServerConnection;

public class GetPeersRequest {

    MainServerConnection mainServerConnection;

    public MainServerConnection getMainServerConnection() {
        return mainServerConnection;
    }

    public void setMainServerConnection(MainServerConnection mainServerConnection) {
        this.mainServerConnection = mainServerConnection;
    }
}
