package com.example.guardiancamera_wifi.domain.models;

public class ProtectedClientStream {

    private static boolean writeLocked = false;

    private static String streamID;
    private static byte protocol;
    private static String videoSrcUrl, audioSrcUrl, geoSrcUrl;
    private static String JWT;


    public static boolean setProtocol(byte newProtocol){
        if (writeLocked)
            return false;
        protocol = newProtocol;
        return true;
    }

    public static boolean setStreamID(String id){
        if (writeLocked)
            return false;
        streamID = id;
        return true;
    }

    public static boolean setJwt(String token){
        if (writeLocked)
            return false;
        JWT = token;
        return true;
    }

    public static boolean setVideoSrcUrl(String url){
        if (writeLocked)
            return false;
        videoSrcUrl = url;
        return true;
    }

    public static boolean setAudioSrcUrl(String url){
        if (writeLocked)
            return false;
        audioSrcUrl = url;
        return true;
    }

    public static boolean setGeoSrcUrl(String url){
        if (writeLocked)
            return false;
        geoSrcUrl = url;
        return true;
    }



    public static byte getProtocol(){
        return protocol;
    }

    public static String getVideoSrcUrl(){
        return videoSrcUrl;
    }

    public static String getAudioSrcUrl(){
        return audioSrcUrl;
    }

    public static String getGeoSrcUrl(){
        return geoSrcUrl;
    }

    public static String getStreamID(){
        return streamID;
    }

    public static String getJWT(){
        return JWT;
    }
}

