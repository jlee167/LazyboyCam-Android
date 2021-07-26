package com.example.guardiancamera_wifi.models;

public class JpegFrame {

    static int SIZE_SOI = 2;

    byte [] soi;
    int size;

    JpegFrame(int size) {
        this.soi = new byte[JpegFrame.SIZE_SOI];
        this.size = size;
    }

    public boolean isSoiDetected() {
        return (soi[1] == (byte)0xff) && (soi[0] == (byte)0xd8);
    }

    public boolean checkByteForSoi(byte nextByte) {
        soi[1] = soi[0];
        soi[0] = nextByte;
        return isSoiDetected();
    }
}
