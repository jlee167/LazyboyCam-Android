package com.example.guardiancamera_wifi.domain.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.guardiancamera_wifi.Env.MAIN_SERVER_URL;


/**
 *      Container class for user information.
 */
public class User {

    private int uid;
    private String firstname;
    private String lastname;
    private String username;
    private String email;
    private String cell;
    private String authProvider;
    private String webToken;
    private String streamAddress;
    private String status;
    private String profileImageUrl;
    private Bitmap profileImage;


    public User(JSONObject user) throws JSONException {
        this.profileImageUrl = "http://" + MAIN_SERVER_URL + (String) user.get("image_url");
        this.uid        = (int) user.get("id");
        this.username   = user.get("username").toString();
        this.email      = user.get("email").toString();
        //this.cell       = user.get("cell").toString();
        this.authProvider   = (String) user.get("auth_provider");
        this.status         = (String) user.get("status");
        this.webToken       = (String) user.get("web_token");
    }

    public User(){};

    public void registerPeerUser(JSONObject user) throws JSONException {
        this.uid        = (int) user.get("id");
        this.username   = user.get("username").toString();
        this.email      = user.get("email").toString();
        this.cell       = user.get("cell").toString();
        this.profileImageUrl = "http://" + MAIN_SERVER_URL + user.get("image_url").toString();
        if (user.has("stream_id")) {
            this.streamAddress  = user.get("stream_id").toString();
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

    public String getWebToken() {
        return webToken;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getUsername() {
        return username;
    }
}
