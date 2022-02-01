package com.example.guardiancamera_wifi.domain.usecase.userProfile;

import com.example.guardiancamera_wifi.MyApplication;
import com.example.guardiancamera_wifi.data.api.http.MainServerConnection;
import com.example.guardiancamera_wifi.domain.model.User;
import com.example.guardiancamera_wifi.domain.usecase.base.UseCase;
import com.example.guardiancamera_wifi.domain.usecase.userProfile.exceptions.UserNotFoundException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class GetUserProfileUseCase implements UseCase<Void, GetUserProfileRequest> {

    @Override
    public Void execute(GetUserProfileRequest request)
            throws UserNotFoundException, ExecutionException, InterruptedException, JSONException {
        MainServerConnection conn = request.getMainServerConn();
        JSONObject serverResp = conn.getSelfProfile();

        if (serverResp.getBoolean("result"))
            MyApplication.currentUser = new User(conn.getSelfProfile());
        else
            throw new UserNotFoundException();

        return null;
    }
}
