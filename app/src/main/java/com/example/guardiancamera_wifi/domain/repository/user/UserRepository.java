package com.example.guardiancamera_wifi.domain.repository.user;

import com.example.guardiancamera_wifi.data.exceptions.auth.AuthFailed;
import com.example.guardiancamera_wifi.domain.model.Peers;
import com.example.guardiancamera_wifi.domain.model.Types;
import com.example.guardiancamera_wifi.domain.model.User;

import org.json.JSONException;

import java.util.concurrent.ExecutionException;

public interface UserRepository {
    boolean nonSocialLogin(String username, String password) throws ExecutionException, InterruptedException, AuthFailed, JSONException;
    boolean oAuthLogin(String accessToken, Types.OAuthProvider authProvider) throws ExecutionException, InterruptedException, AuthFailed;
    User getClientProfile() throws ExecutionException, InterruptedException, JSONException;
    Peers getPeers() throws InterruptedException, ExecutionException, JSONException;
    String getMyJWT() throws ExecutionException, InterruptedException;
}
