package com.example.guardiancamera_wifi.data.api.http;

import com.example.guardiancamera_wifi.domain.model.HttpResponse;
import com.example.guardiancamera_wifi.domain.model.Peers;
import com.example.guardiancamera_wifi.domain.model.Types;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public interface UserApiInterface {

    Peers getPeers() throws JSONException, ExecutionException, InterruptedException;

    HttpResponse oAuthLogin(String accessToken, Types.OAuthProvider authProvider)
            throws ExecutionException, InterruptedException;

    HttpResponse nonSocialLogin(final String username, final String password)
            throws ExecutionException, InterruptedException;

    JSONObject getClientProfile() throws ExecutionException, InterruptedException;

    String getMyJWT() throws ExecutionException, InterruptedException;
}
