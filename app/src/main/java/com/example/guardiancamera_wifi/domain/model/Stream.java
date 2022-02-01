package com.example.guardiancamera_wifi.domain.model;

import com.example.guardiancamera_wifi.data.utils.VideoConfig;

public class Stream {

    private boolean writeLocked = false;

    protected int id;
    protected String videoDestUrl, audioDestUrl, geoDestUrl;
    protected VideoConfig videoConfig;

    
    public VideoConfig getVideoConfig() {
        return videoConfig;
    }

    public void setVideoConfig(VideoConfig videoConfig) {
        this.videoConfig = videoConfig;
    }

    public boolean setVideoDestUrl(String url) {
        if (writeLocked)
            return false;
        videoDestUrl = url;
        return true;
    }

    public boolean setAudioDestUrl(String url) {
        if (writeLocked)
            return false;
        audioDestUrl = url;
        return true;
    }

    public boolean setGeoDestUrl(String url) {
        if (writeLocked)
            return false;
        geoDestUrl = url;
        return true;
    }

    public boolean setId(int id) {
        if (writeLocked)
            return false;
        this.id = id;
        return true;
    }

    public void setWriteLock() {
        writeLocked = true;
    }

    public void releaseWriteLock() {
        writeLocked = false;
    }

    public String getVideoDestUrl() {
        return videoDestUrl;
    }

    public String getAudioDestUrl() {
        return audioDestUrl;
    }

    public String getGeoDestUrl() {
        return geoDestUrl;
    }

    public int getId() {
        return id;
    }
}
