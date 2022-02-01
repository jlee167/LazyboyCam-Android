package com.example.guardiancamera_wifi.domain.usecase.getPeers;

import com.example.guardiancamera_wifi.data.api.http.MainServerConnection;
import com.example.guardiancamera_wifi.domain.model.Peers;
import com.example.guardiancamera_wifi.domain.usecase.base.UseCase;

import org.json.JSONException;

import java.util.concurrent.ExecutionException;

public class GetPeersUseCase implements UseCase<Peers, GetPeersRequest> {

    @Override
    public Peers execute(GetPeersRequest request)
            throws InterruptedException, ExecutionException, JSONException {
        MainServerConnection mainServerConnection;
        Peers peers;

        mainServerConnection = request.getMainServerConnection();
        peers = mainServerConnection.getPeers();

        return peers;
    }
}
