package com.example.guardiancamera_wifi.domain.model;

public class HttpResponse {

    private int code;
    private byte[] body;

    public HttpResponse() {
    }

    public HttpResponse(int code, byte[] body) {
        this.code = code;
        this.body = body;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
