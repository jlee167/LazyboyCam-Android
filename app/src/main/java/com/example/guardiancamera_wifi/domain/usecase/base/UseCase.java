package com.example.guardiancamera_wifi.domain.usecase.base;

import com.example.guardiancamera_wifi.domain.usecase.login.exceptions.InvalidCredentialException;
import com.example.guardiancamera_wifi.domain.usecase.user.exceptions.UserNotFoundException;

import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface UseCase<Result, Params> {
    Result execute(Params params) throws IOException, UserNotFoundException, ExecutionException, InterruptedException, JSONException, InvalidCredentialException;
}
