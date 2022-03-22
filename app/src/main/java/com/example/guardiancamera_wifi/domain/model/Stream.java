package com.example.guardiancamera_wifi.domain.model;

import com.example.guardiancamera_wifi.data.utils.VideoConfig;

public class Stream {

    private boolean writeLocked = false;

    protected int id;
    protected String videoPostUrl, audioPostUrl, geoDataPostUrl;
    protected VideoConfig videoConfig;

    
    public VideoConfig getVideoConfig() {
        return videoConfig;
    }

    public void setVideoConfig(VideoConfig videoConfig) {
        this.videoConfig = videoConfig;
    }

    public boolean setVideoPostUrl(String url) {
        if (writeLocked)
            return false;
        videoPostUrl = url;
        return true;
    }

    public boolean setAudioPostUrl(String url) {
        if (writeLocked)
            return false;
        audioPostUrl = url;
        return true;
    }

    public boolean setGeoDataPostUrl(String url) {
        if (writeLocked)
            return false;
        geoDataPostUrl = url;
        return true;
    }

    public boolean setId(int id) {
        if (writeLocked)
            return false;
        this.id = id;
        return true;
    }

    public String getVideoPostUrl() {
        return videoPostUrl;
    }

    public String getAudioPostUrl() {
        return audioPostUrl;
    }

    public String getGeoDataPostUrl() {
        return geoDataPostUrl;
    }

    public int getId() {
        return id;
    }
}
