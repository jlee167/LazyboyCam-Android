package com.example.guardiancamera_wifi.domain.usecases;

import com.example.guardiancamera_wifi.data.api.http.MainServerConnection;
import com.example.guardiancamera_wifi.domain.libs.types.Types;
import com.example.guardiancamera_wifi.domain.usecases.base.UseCase;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class LoginUseCase implements UseCase {

    public Boolean login(MainServerConnection mainServerConn, Types.OAuthProvider authProvider, String username,
                                    String password) throws IOException {
        try {
            mainServerConn.setAuthProvider(authProvider);
            mainServerConn.registerOAuthAccount();
            boolean result = mainServerConn.login(username, password);

            /* Force delete credential */
            username = null;
            password = null;

            return result;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean logout() {
        //@Todo
        return false;
    }
}
