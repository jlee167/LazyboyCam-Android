package com.example.guardiancamera_wifi.domain.usecase.login;

import com.example.guardiancamera_wifi.data.api.http.MainServer;
import com.example.guardiancamera_wifi.domain.model.Types;

public class LoginRequest {
    private MainServer mainServerConn;
    private Types.OAuthProvider authProvider;
    private String username;
    private String password;
    private String OAuthAccessToken;



    public MainServer getMainServerConn() {
        return mainServerConn;
    }

    public void setMainServerConn(MainServer mainServerConn) {
        this.mainServerConn = mainServerConn;
    }

    public Types.OAuthProvider getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(Types.OAuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccessToken() {
        return OAuthAccessToken;
    }

    public void setOAuthAccessToken(String OAuthAccessToken) {
        this.OAuthAccessToken = OAuthAccessToken;
    }
}
