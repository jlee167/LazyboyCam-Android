package com.example.guardiancamera_wifi.data.repository.stream;

import com.example.guardiancamera_wifi.data.net.http.StreamingServer;
import com.example.guardiancamera_wifi.data.repository.stream.remote.StreamDataSource;

public class StreamRepositoryImpl {

    private StreamDataSource dataSource;

    public StreamRepositoryImpl() {
        this.dataSource = new StreamDataSource(new StreamingServer());
    }
}
