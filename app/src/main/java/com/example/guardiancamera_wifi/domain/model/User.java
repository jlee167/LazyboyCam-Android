package com.example.guardiancamera_wifi.domain.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 *      Container class for user information.
 */
public class User {

    private int uid;
    private String username;
    private String email;
    private String authProvider;
    private String privateKey;
    private String streamID;
    private String status;
    private String profileImageUrl;
    private String streamAccessToken;


    public User(JSONObject user) throws JSONException {
        this.profileImageUrl = (String) user.get("image_url");
        this.uid        = (int) user.get("id");
        this.username   = user.get("username").toString();
        this.email      = user.get("email").toString();
        this.authProvider   = (String) user.get("auth_provider");
        this.status         = (String) user.get("status");
        this.privateKey = (String) user.get("stream_key");
    }

    public User(){};

    public void registerPeerUser(JSONObject user) throws JSONException {
        this.uid        = (int) user.get("id");
        this.username   = user.get("username").toString();
        this.email      = user.get("email").toString();
        this.profileImageUrl = user.get("image_url").toString();
        if (user.has("streamID")) {
            if (!user.isNull("streamID"))
                this.streamID = user.get("streamID").toString();
            this.status = user.get("status").toString();
        }
    }


    public Bitmap getImageBitmap(String imageUrl) throws IOException {
        Bitmap result;
        URL url = new URL(imageUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        conn.connect();
        InputStream in = conn.getInputStream();
        result = BitmapFactory.decodeStream(in);
        return result;
    }


    public int getUid() {
        return uid;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setStreamAccessToken(String token) {
        streamAccessToken = token;
    }

    public String getStreamAccessToken() {
        return streamAccessToken;
    }

    public String getStreamID() {
        return streamID;
    }
}
