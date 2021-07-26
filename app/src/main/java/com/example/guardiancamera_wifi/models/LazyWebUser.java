package com.example.guardiancamera_wifi.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

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

    public String profileImageUrl;
    public Bitmap profileImage;

    public String email;
    public String cell;
    public String authProvider;
    public String streamID;
    public String status;
    public String privacy;
    public String response;
    public boolean proxy;

    public String streamAddress;
    public String userStatus;
    public String[] cameraID;

    /**
     *
     */
    public LazyWebUser() {
    }


    /**
     *
     * @param user
     * @throws JSONException
     */
    public LazyWebUser(JSONObject user) throws JSONException {
            this.profileImageUrl = (String) user.get("id_profile_picture");
            this.uid        = (int) user.get("uid");
            this.username   = (String) user.get("name");
            this.email      = (String) user.get("email");
            this.cell       = (String) user.get("phone");
            this.authProvider   = (String) user.get("authenticator");
            this.streamAddress  = (String) user.get("id_stream");
            this.userStatus     = (String) user.get("status");
            this.cameraID       = TextUtils.split((String) user.get("id_camera"), ";");

            try {
                this.profileImage = getImageBitmap(this.profileImageUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }


    /**
     *
     * @param user
     * @return
     */
    public boolean set(JSONObject user) {
        try {
            this.profileImageUrl = (String) user.get("id_profile_picture");
            this.uid = (int) user.get("uid");
            this.username = (String) user.get("name");
            this.email = (String) user.get("email");
            this.cell = (String) user.get("phone");
            this.authProvider = (String) user.get("authenticator");
            this.streamAddress = (String) user.get("id_stream");
            this.userStatus = (String) user.get("status");
            this.cameraID = TextUtils.split((String) user.get("id_camera"), ";");

        }
        catch (JSONException e) {
            return false;
        }

        try {
            this.profileImage = getImageBitmap(this.profileImageUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }


    public boolean registerPeerUser(JSONObject user) {
        try {
            this.uid        = (int) user.get("id");
            this.username   = user.get("username").toString();
            this.email      = user.get("email").toString();
            this.cell       = user.get("cell").toString();
            this.profileImageUrl = user.get("image_url").toString();
            if (user.has("stream_id")) {
                this.streamAddress  = user.get("stream_id").toString();
                this.userStatus     = user.get("status").toString();
            }
        }
        catch (JSONException e) {
            return false;
        }

        try {
            this.profileImage = getImageBitmap(this.profileImageUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }


    /**
     *
     * @param imageUrl
     * @return
     * @throws IOException
     */
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
