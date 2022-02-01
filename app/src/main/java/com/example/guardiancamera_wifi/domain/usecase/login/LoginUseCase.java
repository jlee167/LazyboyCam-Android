package com.example.guardiancamera_wifi.domain.usecase.login;

import com.example.guardiancamera_wifi.data.api.http.MainServerConnection;
import com.example.guardiancamera_wifi.domain.model.Types;
import com.example.guardiancamera_wifi.domain.usecase.base.UseCase;
import com.example.guardiancamera_wifi.domain.usecase.login.exceptions.InvalidCredentialException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class LoginUseCase implements UseCase<Void, LoginRequest> {

    public Void execute(LoginRequest request)
            throws InterruptedException, ExecutionException, JSONException, InvalidCredentialException {

        MainServerConnection mainServerConn;
        Types.OAuthProvider authProvider;
        JSONObject result;
        String username;
        String password;

        mainServerConn = request.getMainServerConn();
        authProvider = request.getAuthProvider();
        username = request.getUsername();
        password = request.getPassword();

        mainServerConn.clearCookies();
        if (authProvider == Types.OAuthProvider.AUTHENTICATOR_NONSOCIAL)
            result = mainServerConn.nonSocialLogin(username, password);
        else
            result = mainServerConn.oAuthLogin(request.getOAuthAccessToken(), authProvider);

        if (result.getBoolean("token"))
            return null;
        else
            throw new InvalidCredentialException();
    }
}