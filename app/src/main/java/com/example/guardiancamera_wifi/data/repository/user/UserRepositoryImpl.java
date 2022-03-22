package com.example.guardiancamera_wifi.data.repository.user;

import com.example.guardiancamera_wifi.data.api.http.MainServer;
import com.example.guardiancamera_wifi.data.exceptions.auth.AuthFailed;
import com.example.guardiancamera_wifi.domain.model.HttpResponse;
import com.example.guardiancamera_wifi.domain.model.Peers;
import com.example.guardiancamera_wifi.domain.model.Types;
import com.example.guardiancamera_wifi.domain.model.User;
import com.example.guardiancamera_wifi.domain.repository.user.UserRepository;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class UserRepositoryImpl implements UserRepository {

    MainServer mainServer;

    public UserRepositoryImpl() {
        mainServer = new MainServer();
    }

    @Override
    public JSONObject nonSocialLogin(String username, String password)
            throws ExecutionException, InterruptedException {
        return mainServer.nonSocialLogin(username, password);
    }

    @Override
    public JSONObject oAuthLogin(String accessToken, Types.OAuthProvider authProvider)
            throws ExecutionException, InterruptedException {
        return mainServer.oAuthLogin(accessToken, authProvider);
    }

    @Override
    public User getClientProfile() throws ExecutionException, InterruptedException, JSONException {
        return new User(mainServer.getClientProfile());
    }

    public Peers getPeers() throws InterruptedException, ExecutionException, JSONException {
        return mainServer.getPeers();
    }

    public String getMyJWT() throws ExecutionException, InterruptedException {
        return mainServer.getMyJWT();
    }
}
