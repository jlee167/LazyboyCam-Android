package com.example.guardiancamera_wifi.domain.usecase.login;

import com.example.guardiancamera_wifi.domain.model.Types;
import com.example.guardiancamera_wifi.domain.repository.user.UserRepository;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class LoginUseCase {

    private UserRepository userRepository;

    public LoginUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public JSONObject execute(LoginRequest loginRequest)
            throws ExecutionException, InterruptedException {

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
