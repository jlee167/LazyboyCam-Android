package com.example.guardiancamera_wifi.data.configs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;


public class VideoConfig {

    final Context appContext;

    // Frame Size macros
    final static String FRAME_SIZE_QCIF = "QCIF(320 X 240)";
    final static String FRAME_SIZE_VGA = "VGA(640 X 480)";
    final static String FRAME_SIZE_SVGA = "SVGA(800 X 600)";
    final static String FRAME_SIZE_HD = "HD(1280 X 720)";
    final static String FRAME_SIZE_FHD = "FULL HD(1920 X 1080)";

    final static byte ID_QCIF = 0;
    final static byte ID_VGA = 1;
    final static byte ID_SVGA = 2;
    final static byte ID_HD = 3;
    final static byte ID_FHD = 4;

    final static String FORMAT_MJPEG = "MJPEG";
    final static String FORMAT_RGB565 = "RGB565";

    final static byte ID_MJPEG = 0;
    final static byte ID_RGB565 = 1;
    final static byte ID_RTMP = 2;

    public String serialNumber;
    public boolean useExtCamera = false;
    public byte resolution;
    public byte format;


    /**
     *      Constructor with default settings
     *
     *      Todo: Restrict Framesize when RGB565 format is selected
     */
    public VideoConfig(Context context) {
        appContext = context;
    }

    public void update() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        serialNumber = preferences.getString("camSerial", "");
        useExtCamera = preferences.getBoolean("extCamera", false);

        String resPreference = preferences.getString("resolution", "");
        String formatPreference = preferences.getString("format", "");

        assert resPreference != null;
        switch (resPreference) {
            case FRAME_SIZE_QCIF:
                resolution = ID_QCIF;
                break;

            case FRAME_SIZE_VGA:
                resolution = ID_VGA;
                break;

            case FRAME_SIZE_SVGA:
                resolution = ID_SVGA;
                break;

            case FRAME_SIZE_HD:
                resolution = ID_HD;
                break;

            case FRAME_SIZE_FHD:
                resolution = ID_FHD;
                break;

            default:
                resolution = ID_QCIF;
        }

        assert formatPreference != null;
        if (useExtCamera)
            format = ID_RTMP;
        else {
            switch (formatPreference) {
                case FORMAT_MJPEG:
                    format = ID_MJPEG;
                    break;

                case FORMAT_RGB565:
                    format = ID_RGB565;
                    break;

                default:
                    format = ID_MJPEG;
            }
        }
    }
}
