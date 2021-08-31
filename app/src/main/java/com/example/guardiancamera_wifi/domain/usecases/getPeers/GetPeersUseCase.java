package com.example.guardiancamera_wifi.domain.usecases.getPeers;

import com.example.guardiancamera_wifi.data.api.http.MainServerConnection;
import com.example.guardiancamera_wifi.domain.models.LazyWebPeers;
import com.example.guardiancamera_wifi.domain.usecases.base.UseCase;

import org.json.JSONException;

import java.util.concurrent.ExecutionException;

public class GetPeersUseCase implements UseCase<LazyWebPeers, GetPeersRequest> {

    @Override
    public LazyWebPeers execute(GetPeersRequest request)
            throws InterruptedException, ExecutionException, JSONException {
        MainServerConnection mainServerConnection;
        LazyWebPeers peers;

        mainServerConnection = request.getMainServerConnection();
        peers = mainServerConnection.getPeers();

        return peers;
    }
}
