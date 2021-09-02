package com.example.guardiancamera_wifi.domain.models;

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
public class LazyWebUser {

    public int uid;
    public String firstname;
    public String lastname;
    public String username;
    public String email;
    public String cell;
    public String authProvider;
    public String webToken;
    public String streamAddress;
    public String status;

    public String profileImageUrl;
    public Bitmap profileImage;


    public LazyWebUser(JSONObject user) throws JSONException {
        this.profileImageUrl = (String) user.get("id_profile_picture");
        this.uid        = (int) user.get("uid");
        this.username   = user.get("username").toString();
        this.email      = user.get("email").toString();
        this.cell       = user.get("cell").toString();
        this.authProvider   = (String) user.get("authenticator");
        this.status         = (String) user.get("status");
        this.webToken       = (String) user.get("web_token");

        try {
            this.profileImage = getImageBitmap(this.profileImageUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LazyWebUser(){};

    public void registerPeerUser(JSONObject user) throws JSONException {
        this.uid        = (int) user.get("id");
        this.username   = user.get("username").toString();
        this.email      = user.get("email").toString();
        this.cell       = user.get("cell").toString();
        this.profileImageUrl = user.get("image_url").toString();
        if (user.has("stream_id")) {
            this.streamAddress  = user.get("stream_id").toString();
            this.status = user.get("status").toString();
        }
        try {
            this.profileImage = getImageBitmap(this.profileImageUrl);
        } catch (IOException e) {
            e.printStackTrace();
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
}
