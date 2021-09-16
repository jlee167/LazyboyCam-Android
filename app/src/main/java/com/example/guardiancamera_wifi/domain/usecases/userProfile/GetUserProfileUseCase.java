package com.example.guardiancamera_wifi.domain.usecases.userProfile;

import com.example.guardiancamera_wifi.MyApplication;
import com.example.guardiancamera_wifi.data.api.http.MainServerConnection;
import com.example.guardiancamera_wifi.domain.models.LazyWebUser;
import com.example.guardiancamera_wifi.domain.usecases.base.UseCase;
import com.example.guardiancamera_wifi.domain.usecases.userProfile.exceptions.UserNotFoundException;

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
            MyApplication.currentUser = new LazyWebUser(conn.getSelfProfile());
        else
            throw new UserNotFoundException();

        return null;
    }
}
