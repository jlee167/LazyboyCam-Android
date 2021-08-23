package com.example.guardiancamera_wifi.domain.models;

public class EmergencyStream {

    public static boolean writeLocked = false;

    private static int id;
    private static String videoDestUrl, audioDestUrl, geoDestUrl;


    public static boolean setVideoDestUrl(String url) {
        if (writeLocked)
            return false;
        EmergencyStream.videoDestUrl = url;
        return true;
    }

    public static boolean setAudioDestUrl(String url) {
        if (writeLocked)
            return false;
        EmergencyStream.audioDestUrl = url;
        return true;
    }

    public static boolean setGeoDestUrl(String url) {
        if (writeLocked)
            return false;
        EmergencyStream.geoDestUrl = url;
        return true;
    }

    public static boolean setId(int id) {
        if (writeLocked)
            return false;
        EmergencyStream.id = id;
        return true;
    }

    public static void setWriteLock() {
        EmergencyStream.writeLocked = true;
    }

    public static void releaseWriteLock() {
        EmergencyStream.writeLocked = false;
    }

    public static String getVideoDestUrl() {
        return EmergencyStream.videoDestUrl;
    }

    public static String getAudioDestUrl() {
        return EmergencyStream.audioDestUrl;
    }

    public static String getGeoDestUrl() {
        return EmergencyStream.geoDestUrl;
    }

    public static int getId() {
        return id;
    }
}
