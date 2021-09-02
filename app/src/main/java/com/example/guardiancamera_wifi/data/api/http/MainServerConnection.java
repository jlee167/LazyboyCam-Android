package com.example.guardiancamera_wifi.data.api.http;

import android.text.TextUtils;
import android.util.Log;

import com.example.guardiancamera_wifi.data.api.http.base.HttpConnection;
import com.example.guardiancamera_wifi.data.configs.Addresses;
import com.example.guardiancamera_wifi.data.configs.LazyWebURI;
import com.example.guardiancamera_wifi.domain.models.Types;
import com.example.guardiancamera_wifi.domain.models.LazyWebPeers;
import com.example.guardiancamera_wifi.domain.models.LazyWebUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class MainServerConnection {

    private static boolean session_enabled;
    private static String COOKIES;
    private CookieManager cookieManager;
    private ExecutorService executor;


    public MainServerConnection() {
        session_enabled = false;
        COOKIES = "";
        cookieManager = new CookieManager();
        executor = Executors.newFixedThreadPool(2);
    }


    public void pingServer() throws InterruptedException {
        Thread pingThread = new Thread(new Runnable() {
            String url = LazyWebURI.URI_PING();
            JSONObject token_json = new JSONObject();
            String method = HttpConnection.GET;

            @Override
            public void run() {
                try {
                    sendHttpRequest(url, token_json, method);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        pingThread.start();
        pingThread.join();
    }


    public JSONObject getNonSocialCredential(String username, String password)
            throws JSONException {
        JSONObject authCredential = new JSONObject();
        authCredential.put("username", username);
        authCredential.put("password", password);
        return authCredential;
    }

    public JSONObject getOAuthCredential(String accessToken) throws JSONException {
        JSONObject authCredential = new JSONObject();
        authCredential.put("accessToken", accessToken);
        return authCredential;
    }


    public String sendHttpRequest(String uri, JSONObject body, String httpMethod) throws IOException {

        BufferedOutputStream outputStream;
        BufferedInputStream inputStream;

        String urlString = Addresses.PREFIX_HTTP + Addresses.MAIN_SERVER_URL + uri;
        URL authServerUrl = new URL(urlString);
        HttpURLConnection httpConn;
        boolean outputEnabled = httpMethod.equals(HttpConnection.POST) || httpMethod.equals(HttpConnection.PUT);

        try {
            httpConn = (HttpURLConnection) authServerUrl.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

        if (session_enabled) {
            httpConn.setRequestProperty("Cookie", COOKIES);
        }
        httpConn.setRequestProperty("Content-Type", "application/json; utf-8");
        httpConn.setRequestProperty("Accept", "application/json");
        try {
            httpConn.setRequestMethod(httpMethod);
        } catch (ProtocolException e) {
            e.printStackTrace();
        }

        if (outputEnabled)
            httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        httpConn.connect();

        if (outputEnabled) {
            try {
                outputStream = new BufferedOutputStream(httpConn.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
            outputStream.write(body.toString().getBytes());
            outputStream.flush();
            outputStream.close();
            //streamToAuthServer.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }


        int responseCode = httpConn.getResponseCode();
        String msg = httpConn.getResponseMessage();
        inputStream = new BufferedInputStream(httpConn.getInputStream());
        byte[] response = new byte[1000];
        inputStream.read(response);


        Map<String, List<String>> header = httpConn.getHeaderFields();
        if (header.containsKey("Set-Cookie")) {
            List<String> cookie = header.get("Set-Cookie");
            for (int i = 0; i < cookie.size(); i++) {
                cookieManager.getCookieStore().add(
                        URI.create(Addresses.PREFIX_HTTP + Addresses.MAIN_SERVER_URL),
                        HttpCookie.parse(cookie.get(i)).get(0));
            }
            session_enabled = true;
            COOKIES = TextUtils.join(
                    ";",
                    cookieManager.getCookieStore().get(URI.create(Addresses.PREFIX_HTTP
                            + Addresses.MAIN_SERVER_URL)));
        } else {
            session_enabled = false;
        }
        Log.i("res", new String(response));
        return new String(response);
    }

    public String getAuthUrl(Types.OAuthProvider authProvider) {
        switch (authProvider) {
            case AUTHENTICATOR_KAKAO:
                return LazyWebURI.URI_LOGIN() + LazyWebURI.URI_KAKAO();

            case AUTHENTICATOR_GOOGLE:
                return LazyWebURI.URI_LOGIN() + LazyWebURI.URI_GOOGLE();

            default: {
                return LazyWebURI.URI_LOGIN();
            }
        }
    }

    public JSONObject oAuthLogin(String accessToken, Types.OAuthProvider authProvider)
            throws ExecutionException, InterruptedException {

        Callable<JSONObject> task = new Callable<JSONObject>() {
            private String mAccessToken = accessToken;

            @Override
            public JSONObject call() throws Exception {
                try {
                    String uri = getAuthUrl(authProvider);
                    JSONObject credential = getOAuthCredential(mAccessToken);
                    String result = sendHttpRequest(uri, credential, HttpConnection.POST);
                    return new JSONObject(result);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        };

        Future<JSONObject> future = executor.submit(task);
        return future.get();
    }


    public JSONObject nonSocialLogin(final String username, final String password)
            throws ExecutionException, InterruptedException {

        Callable<JSONObject> task = () -> {
            try {
                String uri = getAuthUrl(Types.OAuthProvider.AUTHENTICATOR_NONSOCIAL);
                JSONObject credential = getNonSocialCredential(username, password);
                String result = sendHttpRequest(uri, credential, HttpConnection.POST);
                return new JSONObject(result);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                throw e;
            }
        };

        Future<JSONObject> future = executor.submit(task);
        return future.get();
    }


    /**
     *  Connect to the authentication server (Http protocol)
     *  Pass Google or Kakao authentication token to the server for validation.
     *
     * @return
     * @throws JSONException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public LazyWebPeers getPeers() throws JSONException, ExecutionException, InterruptedException {

        JSONArray guardiansQueryResult;
        JSONArray protectedsQueryResult;

        Callable<JSONArray[]> guardianRequestTask = new Callable<JSONArray[]>() {
            String uri_guardian = LazyWebURI.URI_GUARDIAN();
            String uri_protected = LazyWebURI.URI_PROTECTED();
            String method = HttpConnection.GET;

            @Override
            public JSONArray[] call() throws Exception {
                return new JSONArray[]{
                        new JSONArray(sendHttpRequest(uri_guardian, new JSONObject(), method)),
                        new JSONArray(sendHttpRequest(uri_protected, new JSONObject(), method))
                };
            }
        };

        Future<JSONArray[]> future = executor.submit(guardianRequestTask);
        guardiansQueryResult = future.get()[0];
        protectedsQueryResult = future.get()[1];
        LazyWebPeers peerGroups = new LazyWebPeers();
        LazyWebUser[] protecteds, guardians;

        protecteds = new LazyWebUser[protectedsQueryResult.length()];
        guardians = new LazyWebUser[guardiansQueryResult.length()];


        for (int i = 0; i < guardiansQueryResult.length(); i++) {
            guardians[i] = new LazyWebUser();
            guardians[i].registerPeerUser(guardiansQueryResult.getJSONObject(i));
        }

        for (int i = 0; i < protectedsQueryResult.length(); i++) {
            protecteds[i] = new LazyWebUser();
            protecteds[i].registerPeerUser(protectedsQueryResult.getJSONObject(i));
        }
        peerGroups.setGuardians(guardians);
        peerGroups.setProtecteds(protecteds);
        return peerGroups;
    }


    public JSONObject getUser() throws ExecutionException, InterruptedException {
        Callable<JSONObject> task = () -> {
            String uri = LazyWebURI.URI_USER();
            try {
                String result = sendHttpRequest(uri, new JSONObject(), HttpConnection.POST);
                return new JSONObject(result);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                throw e;
            }
        };

        Future<JSONObject> future = executor.submit(task);
        return future.get();
    }

    /*
    public void registerUser() throws IOException, JSONException {
        // @todo: fill json
        JSONObject token_json = new JSONObject();
        String uri = LazyWebURI.URI_USER();
        sendHttpRequest(uri, token_json, httpConnection.POST);
    }


    public void updateUser() throws IOException, JSONException {
        // @todo: fill json
        JSONObject token_json = new JSONObject();
        String uri = LazyWebURI.URI_USER();
        sendHttpRequest(uri, token_json, httpConnection.PUT);
    }

    public void deleteUser() throws IOException, JSONException {
        JSONObject token_json = new JSONObject();
        String uri = LazyWebURI.URI_USER();
        sendHttpRequest(uri, token_json, httpConnection.DELETE);
    }
    */
}