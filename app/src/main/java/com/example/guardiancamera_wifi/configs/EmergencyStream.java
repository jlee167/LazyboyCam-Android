package com.example.guardiancamera_wifi.configs;

public class EmergencyStream {

    public static boolean writeLocked = false;

    //private static byte protocol;
    private static String videoDestUrl, audioDestUrl, geoDestUrl;

    /*
    public static boolean setProtocol(byte protocol){
        if (writeLocked)
            return false;
        EmergencyStream.protocol = protocol;
        return true;
    }
    */

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

    public static void setWriteLock() {
        EmergencyStream.writeLocked = true;
    }

    public static void releaseWriteLock() {
        EmergencyStream.writeLocked = false;
    }


    /*
    public static byte getProtocol(){
        return EmergencyStream.protocol;
    }
    */

    public static String getVideoDestUrl() {
        return EmergencyStream.videoDestUrl;
    }

    public static String getAudioDestUrl() {
        return EmergencyStream.audioDestUrl;
    }

    public static String getGeoDestUrl() {
        return EmergencyStream.geoDestUrl;
    }
}
