package com.example.guardiancamera_wifi.domain.repository.user;

import com.example.guardiancamera_wifi.domain.model.Peers;
import com.example.guardiancamera_wifi.domain.model.Types;
import com.example.guardiancamera_wifi.domain.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public interface UserRepository {
    JSONObject nonSocialLogin(String username, String password) throws ExecutionException, InterruptedException;
    JSONObject oAuthLogin(String accessToken, Types.OAuthProvider authProvider) throws ExecutionException, InterruptedException;
    User getClientProfile() throws ExecutionException, InterruptedException, JSONException;
    Peers getPeers() throws InterruptedException, ExecutionException, JSONException;
    String getMyJWT() throws ExecutionException, InterruptedException;
}
