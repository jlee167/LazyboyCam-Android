package com.example.guardiancamera_wifi.data.repository.stream.remote;

import com.example.guardiancamera_wifi.data.net.http.StreamApiInterface;
import com.example.guardiancamera_wifi.data.exceptions.RequestDeniedException;
import com.example.guardiancamera_wifi.data.utils.VideoConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class StreamDataSource {

    private StreamApiInterface streamApiInterface;

    public StreamDataSource(StreamApiInterface streamApiInterface) {
        this.streamApiInterface = streamApiInterface;
    }

    JSONObject startStream(VideoConfig videoConfig)
            throws IOException, JSONException, RequestDeniedException {
        return this.streamApiInterface.startStream(videoConfig);
    }


    JSONObject stopStream() throws IOException, JSONException, RequestDeniedException {
        return this.streamApiInterface.stopStream();
    }

    JSONObject startEmergency() throws IOException, JSONException, RequestDeniedException {
        return this.streamApiInterface.startEmergency();
    }

    JSONObject stopEmergency() throws IOException, JSONException, RequestDeniedException {
        return this.streamApiInterface.stopEmergency();
    }

    void sendLocation(JSONObject location) throws JSONException, IOException, RequestDeniedException {
        this.streamApiInterface.sendLocation(location);
    }
}
