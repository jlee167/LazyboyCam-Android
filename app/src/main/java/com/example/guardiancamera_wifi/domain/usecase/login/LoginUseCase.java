package com.example.guardiancamera_wifi.domain.usecase.login;

import com.example.guardiancamera_wifi.data.exceptions.auth.AuthFailed;
import com.example.guardiancamera_wifi.domain.model.Types;
import com.example.guardiancamera_wifi.domain.repository.user.UserRepository;

import org.json.JSONException;

import java.util.concurrent.ExecutionException;

public class LoginUseCase {

    private final UserRepository userRepository;

    public LoginUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean execute(LoginRequest loginRequest)
            throws ExecutionException, InterruptedException, JSONException, AuthFailed {

        if (loginRequest.getAuthProvider() == Types.OAuthProvider.AUTHENTICATOR_NONSOCIAL) {
            return userRepository.nonSocialLogin(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            );
        }
        else {
            return userRepository.oAuthLogin(
                    loginRequest.getAccessToken(),
                    loginRequest.getAuthProvider()
            );
        }
    }
}
