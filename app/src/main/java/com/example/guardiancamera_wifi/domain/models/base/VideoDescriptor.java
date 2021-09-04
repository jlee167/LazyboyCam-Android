package com.example.guardiancamera_wifi.domain.models.base;


public class VideoDescriptor {

    public enum Format {
        MJPEG,
        RTMP,
        HLS,
        RGB565
    }

    public enum Size{
        QCIF,
        VGA,
        SVGA,
        HD,
        FULLHD
    }

    private Format format;
    private Size size;

    public void setFormat(Format format) {
        this.format = format;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public Format getFormat() {
        return format;
    }

    public Size getSize() {
        return size;
    }
}
