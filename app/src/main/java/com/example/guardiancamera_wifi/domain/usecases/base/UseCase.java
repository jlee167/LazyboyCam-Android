package com.example.guardiancamera_wifi.domain.usecases.base;

import com.example.guardiancamera_wifi.domain.usecases.login.exceptions.InvalidCredentialException;
import com.example.guardiancamera_wifi.domain.usecases.userProfile.exceptions.UserNotFoundException;

import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface UseCase<Result, Params> {
    Result execute(Params params) throws IOException, UserNotFoundException, ExecutionException, InterruptedException, JSONException, InvalidCredentialException;
}
