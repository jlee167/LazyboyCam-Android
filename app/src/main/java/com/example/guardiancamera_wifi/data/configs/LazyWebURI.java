package com.example.guardiancamera_wifi.data.configs;


public class LazyWebURI {

    // Constants used to generate URI
    private static final String URI_LOGIN = "/auth";
    private static final String URI_LOGOUT = "/logout";
    private static final String URI_PING = "/ping";

    private static final String URI_USER_INFO = "/self";
    private static final String URI_PREFIX_USERS = "/members";
    private static final String URI_PREFIX_GUARDIAN = "/members/guardian";
    private static final String URI_PREFIX_PROTECTED = "/members/protected";
    private static final String UID_ALL = "/all";

    private static final String URI_KAKAO = "/kakao";
    private static final String URI_GOOGLE = "/google";


    /**
     * Default Constructor
     */
    public LazyWebURI() {

    }

    public static String URI_PING() {
        return URI_PING;
    }

    public static String URI_LOGIN() {
        return URI_LOGIN;
    }

    public static String URI_LOGOUT() {
        return URI_LOGOUT;
    }

    public static String URI_USER() {
        return URI_USER_INFO;
    }

    public static String URI_KAKAO() {
        return URI_KAKAO;
    }

    public static String URI_GOOGLE() {
        return URI_GOOGLE;
    }

    public static String URI_GUARDIAN(String uid) {
        return URI_PREFIX_GUARDIAN + '/' + uid;
    }

    public static String URI_GUARDIAN() {
        return URI_PREFIX_GUARDIAN + UID_ALL;
    }

    public static String URI_PROTECTED(String uid) {
        return URI_PREFIX_PROTECTED + '/' + uid;
    }

    public static String URI_PROTECTED() {
        return URI_PREFIX_PROTECTED + UID_ALL;
    }
}
