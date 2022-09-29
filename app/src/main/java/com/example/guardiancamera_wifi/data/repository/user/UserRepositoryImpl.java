package com.example.guardiancamera_wifi.data.repository.user;

import com.example.guardiancamera_wifi.data.net.http.MainServer;
import com.example.guardiancamera_wifi.data.exceptions.auth.AuthFailed;
import com.example.guardiancamera_wifi.data.repository.user.remote.UserDataSource;
import com.example.guardiancamera_wifi.domain.model.HttpResponse;
import com.example.guardiancamera_wifi.domain.model.Peers;
import com.example.guardiancamera_wifi.domain.model.Types;
import com.example.guardiancamera_wifi.domain.model.User;
import com.example.guardiancamera_wifi.domain.repository.user.UserRepository;

import org.json.JSONException;
import java.util.concurrent.ExecutionException;


public class UserRepositoryImpl implements UserRepository {

    UserDataSource userDataSource;

    public UserRepositoryImpl() {
        userDataSource = new UserDataSource(new MainServer());
    }

    @Override
    public boolean nonSocialLogin(String username, String password)
            throws ExecutionException, InterruptedException, AuthFailed {
        try {
            HttpResponse response = userDataSource.nonSocialLogin(username, password);
            if (response.getCode() != 200) {
                throw new AuthFailed();
            } else {
                return true;
            }
        } catch (Exception e) {
            userDataSource.clean();
            throw e;
        }
    }

    @Override
    public boolean oAuthLogin(String accessToken, Types.OAuthProvider authProvider)
            throws ExecutionException, InterruptedException, AuthFailed {
        try {
            HttpResponse response = userDataSource.oAuthLogin(accessToken, authProvider);
            if (response.getCode() != 200) {
                throw new AuthFailed();
            } else {
                return true;
            }
        } catch (Exception e) {
            userDataSource.clean();
            throw e;
        }
    }

    @Override
    public User getClientProfile() throws ExecutionException, InterruptedException, JSONException {
        return new User(userDataSource.getClientProfile());
    }

    public Peers getPeers() throws InterruptedException, ExecutionException, JSONException {
        return userDataSource.getPeers();
    }

    public String getMyJWT() throws ExecutionException, InterruptedException {
        return userDataSource.getMyJWT();
    }
}
