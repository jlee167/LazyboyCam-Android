package com.example.guardiancamera_wifi.configs;

public class WifiCameraProtocol {
    public static final byte [] CAM_CMD_PREAMBLE = {(byte)0x24, (byte)0x73};

    public static final byte CAM_REQUEST_EMERGENCY = 0x10;
    public static final byte CAM_CMD_STOP_EMERGENCY = 0x11;
    public static final byte CAM_CMD_SET_FRAMESIZE = 0X13;
    public static final byte CAM_CMD_DISCONNECT = 0x15;

    public static final byte CAM_REQUEST_SERIAL_ID = 0x12;

    public static final byte CAM_RESP_ACK = 0x41;
    public static final byte CAM_RESP_ERR = 0x42;
}
