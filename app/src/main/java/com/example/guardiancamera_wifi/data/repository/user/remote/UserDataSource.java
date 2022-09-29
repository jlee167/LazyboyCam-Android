package com.example.guardiancamera_wifi.data.repository.user.remote;

import com.example.guardiancamera_wifi.data.api.http.UserApiInterface;
import com.example.guardiancamera_wifi.domain.model.HttpResponse;
import com.example.guardiancamera_wifi.domain.model.Peers;
import com.example.guardiancamera_wifi.domain.model.Types;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class UserDataSource {

    private UserApiInterface apiInterface;

    public UserDataSource(UserApiInterface apiInterface) {
        this.apiInterface = apiInterface;
    }

    public Peers getPeers() throws JSONException, ExecutionException, InterruptedException {
        return this.apiInterface.getPeers();
    }

    public HttpResponse oAuthLogin(String accessToken, Types.OAuthProvider authProvider)
            throws ExecutionException, InterruptedException
    {
        return this.apiInterface.oAuthLogin(accessToken, authProvider);
    };

    public HttpResponse nonSocialLogin(final String username, final String password)
            throws ExecutionException, InterruptedException
    {
        return this.apiInterface.nonSocialLogin(username, password);
    };

    public JSONObject getClientProfile() throws ExecutionException, InterruptedException
    {
        return this.apiInterface.getClientProfile();
    };

    public String getMyJWT() throws ExecutionException, InterruptedException
    {
        return this.apiInterface.getMyJWT();
    };

    public void clean() {
        this.apiInterface.clearCookies();
    }
}
