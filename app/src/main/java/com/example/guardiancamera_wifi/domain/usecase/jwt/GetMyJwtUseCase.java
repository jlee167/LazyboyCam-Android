package com.example.guardiancamera_wifi.domain.usecase.jwt;

import com.example.guardiancamera_wifi.domain.repository.user.UserRepository;

import org.json.JSONException;

import java.util.concurrent.ExecutionException;

public class GetMyJwtUseCase {

    UserRepository userRepository;

    public GetMyJwtUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String execute()
            throws ExecutionException, InterruptedException, JSONException {

        return userRepository.getMyJWT();
    }
}
