package com.example.guardiancamera_wifi.domain.models;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;


public class VideoConfig {

    // Frame Size macros
    final static String FRAME_SIZE_QCIF = "QCIF(320 X 240)";
    final static String FRAME_SIZE_VGA = "VGA(640 X 480)";
    final static String FRAME_SIZE_SVGA = "SVGA(800 X 600)";
    final static String FRAME_SIZE_HD = "HD(1280 X 720)";
    final static String FRAME_SIZE_FHD = "FULL HD(1920 X 1080)";

    final static byte IDX_QCIF    = 0;
    final static byte IDX_VGA     = 1;
    final static byte IDX_SVGA    = 2;
    final static byte IDX_HD      = 3;
    final static byte IDX_FHD     = 4;

    final static String FORMAT_MJPEG = "MJPEG";
    final static String FORMAT_RGB565 = "RGB565";

    final static byte IDX_MJPEG     = 0;
    final static byte IDX_RGB565    = 1;
    final static byte IDX_RTMP      = 2;

    public static String serialNumber;
    public static boolean useExtCamera = false;
    public static byte resolution;
    public static byte format;


    /**
     *      Constructor with default settings
     *
     *      Todo: Restrict Framesize when RGB565 format is selected
     */
    public VideoConfig(Context context) {
    }

    public static void update(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        serialNumber = preferences.getString("camSerial", "");
        useExtCamera = preferences.getBoolean("extCamera", false);

        String resPreference = preferences.getString("resolution", "");
        String formatPreference = preferences.getString("format", "");

        assert resPreference != null;
        switch (resPreference) {
            case FRAME_SIZE_QCIF:
                resolution = IDX_QCIF;
                break;

            case FRAME_SIZE_VGA:
                resolution = IDX_VGA;
                break;

            case FRAME_SIZE_SVGA:
                resolution = IDX_SVGA;
                break;

            case FRAME_SIZE_HD:
                resolution = IDX_HD;
                break;

            case FRAME_SIZE_FHD:
                resolution = IDX_FHD;
                break;

            default:
                resolution = IDX_QCIF;
        }

        assert formatPreference != null;
        if (useExtCamera)
            format = IDX_RTMP;
        else {
            switch (formatPreference) {
                case FORMAT_MJPEG:
                    format = IDX_MJPEG;
                    break;

                case FORMAT_RGB565:
                    format = IDX_RGB565;
                    break;

                default:
                    format = IDX_MJPEG;
            }
        }
    }
}
