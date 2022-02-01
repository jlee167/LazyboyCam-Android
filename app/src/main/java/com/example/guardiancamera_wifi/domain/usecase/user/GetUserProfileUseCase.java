package com.example.guardiancamera_wifi.domain.usecase.user;

import com.example.guardiancamera_wifi.domain.model.User;
import com.example.guardiancamera_wifi.domain.repository.user.UserRepository;
import com.example.guardiancamera_wifi.domain.usecase.user.exceptions.UserNotFoundException;

import org.json.JSONException;

import java.util.concurrent.ExecutionException;

public class GetUserProfileUseCase {

    UserRepository userRepository;

    public GetUserProfileUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User execute()
            throws UserNotFoundException, ExecutionException, InterruptedException, JSONException {

        User user = userRepository.getClientProfile();
        return user;
    }
}
