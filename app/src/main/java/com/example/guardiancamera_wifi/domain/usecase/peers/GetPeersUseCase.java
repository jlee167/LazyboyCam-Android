package com.example.guardiancamera_wifi.domain.usecase.peers;

import com.example.guardiancamera_wifi.domain.model.Peers;
import com.example.guardiancamera_wifi.domain.repository.user.UserRepository;

import org.json.JSONException;

import java.util.concurrent.ExecutionException;

public class GetPeersUseCase {

    private UserRepository userRepository;

    public GetPeersUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Peers execute()
            throws InterruptedException, ExecutionException, JSONException {
        return userRepository.getPeers();
    }
}
