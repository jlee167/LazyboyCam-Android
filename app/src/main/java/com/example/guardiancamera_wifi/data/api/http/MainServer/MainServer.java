package com.example.guardiancamera_wifi.data.api.http.MainServer;

import android.text.TextUtils;
import android.util.Log;

import com.example.guardiancamera_wifi.Env;
import com.example.guardiancamera_wifi.data.utils.HttpConnection;
import com.example.guardiancamera_wifi.data.utils.LazyWebURI;
import com.example.guardiancamera_wifi.data.utils.URI;
import com.example.guardiancamera_wifi.domain.model.HttpResponse;
import com.example.guardiancamera_wifi.domain.model.Peers;
import com.example.guardiancamera_wifi.domain.model.Types;
import com.example.guardiancamera_wifi.domain.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class MainServer extends HttpConnection implements UserApiInterface{

    private static boolean session_enabled;
    public static String COOKIES;
    public static CookieManager cookieManager;
    private static ExecutorService executor;

    static {
        cookieManager = new CookieManager();
        executor = Executors.newFixedThreadPool(Env.MAX_THREADS_MAIN_SERVER);
    }

    public MainServer() {
        session_enabled = false;
        clearCookies();
    }

    @Override
    public HttpResponse sendHttpRequest(String url, JSONObject header, JSONObject body, String method)
            throws IOException {
        BufferedOutputStream outputStream;
        BufferedInputStream inputStream;
        URL authServerUrl;
        HttpURLConnection httpConn;
        boolean outputEnabled;
        HttpResponse response;

        authServerUrl = new URL(url);
        httpConn = (HttpURLConnection) authServerUrl.openConnection();
        outputEnabled = method.equals(POST) || method.equals(PUT);
        response = new HttpResponse();

        if (session_enabled) {
            httpConn.setRequestProperty("Cookie", COOKIES);
        }
        httpConn.setRequestProperty("Content-Type", "application/json");
        httpConn.setRequestProperty("Accept", "application/json");
        httpConn.setRequestProperty("Connection", "close");
        httpConn.setRequestMethod(method);

        if (outputEnabled) {
            httpConn.setDoOutput(true);
        }
        httpConn.setDoInput(true);
        httpConn.connect();

        if (outputEnabled) {
            outputStream = new BufferedOutputStream(httpConn.getOutputStream());
            outputStream.write(body.toString().getBytes());
            outputStream.flush();
            outputStream.close();
        }

        response.setCode(httpConn.getResponseCode());
        byte[] responseBody;
        ByteArrayOutputStream bufStream =  new ByteArrayOutputStream();

        try {
            inputStream = new BufferedInputStream(httpConn.getInputStream());
            int contentLength = httpConn.getContentLength();
            while (bufStream.size() < contentLength) {
                responseBody = new byte[contentLength];
                inputStream.read(responseBody);
                bufStream.write(responseBody);
            }
            response.setBody(bufStream.toByteArray());
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            throw e;
        }


        Map<String, List<String>> headers = httpConn.getHeaderFields();
        if (headers.containsKey("Set-Cookie")) {
            List<String> cookie = headers.get("Set-Cookie");
            for (int i = 0; i < cookie.size(); i++) {
                cookieManager.getCookieStore().add(
                        java.net.URI.create(URI.PREFIX_HTTP + Env.MAIN_SERVER_URL),
                        HttpCookie.parse(cookie.get(i)).get(0));
            }
            session_enabled = true;
            COOKIES = TextUtils.join(
                    ";",
                    cookieManager.getCookieStore().get(java.net.URI.create(URI.PREFIX_HTTP
                            + Env.MAIN_SERVER_URL)));
        } else {
            session_enabled = false;
        }
        return response;
    }


    public void clearCookies() {
        COOKIES = "";
        cookieManager.getCookieStore().removeAll();
    }


    public void pingServer() throws InterruptedException {
        Thread pingThread = new Thread(new Runnable() {
            String url = LazyWebURI.URI_PING();
            String method = HttpConnection.GET;

            @Override
            public void run() {
                try {
                    sendHttpRequest(url, new JSONObject(), new JSONObject(), method);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        pingThread.start();
        pingThread.join();
    }


    public JSONObject getCredentials(String username, String password)
            throws JSONException {
        JSONObject authCredential = new JSONObject();
        authCredential.put("username", username);
        authCredential.put("password", password);
        return authCredential;
    }


    public JSONObject getOAuthToken(String accessToken) throws JSONException {
        JSONObject authCredential = new JSONObject();
        authCredential.put("accessToken", accessToken);
        return authCredential;
    }



    public String getAuthUrl(Types.OAuthProvider authProvider) {
        switch (authProvider) {
            case AUTHENTICATOR_KAKAO:
                return URI.PREFIX_HTTP + Env.MAIN_SERVER_IP + LazyWebURI.URI_LOGIN() + LazyWebURI.URI_KAKAO();

            case AUTHENTICATOR_GOOGLE:
                return URI.PREFIX_HTTP + Env.MAIN_SERVER_IP + LazyWebURI.URI_LOGIN() + LazyWebURI.URI_GOOGLE();

            default: {
                return URI.PREFIX_HTTP + Env.MAIN_SERVER_IP + LazyWebURI.URI_LOGIN();
            }
        }
    }


    public HttpResponse oAuthLogin(String accessToken, Types.OAuthProvider authProvider)
            throws ExecutionException, InterruptedException {

        Callable<HttpResponse> task = new Callable<HttpResponse>() {
            private String mAccessToken = accessToken;

            @Override
            public HttpResponse call() throws Exception {
                try {
                    String uri = getAuthUrl(authProvider);
                    JSONObject credential = getOAuthToken(mAccessToken);
                    return sendHttpRequest(uri, new JSONObject(), credential, HttpConnection.POST);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        };

        Future<HttpResponse> future = executor.submit(task);
        return future.get();
    }


    public HttpResponse nonSocialLogin(final String username, final String password)
            throws ExecutionException, InterruptedException {

        Callable<HttpResponse> task = () -> {
            String uri = getAuthUrl(Types.OAuthProvider.AUTHENTICATOR_NONSOCIAL);
            JSONObject credential = getCredentials(username, password);
            return sendHttpRequest(uri, new JSONObject(), credential, HttpConnection.POST);
        };

        Future<HttpResponse> future = executor.submit(task);
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
    public Peers getPeers() throws JSONException, ExecutionException, InterruptedException {

        HttpResponse getGuardiansResponse;
        HttpResponse getProtectedsResponse;

        Callable<HttpResponse[]> guardianRequestTask = new Callable<HttpResponse[]>() {
            String uri_guardian = URI.PREFIX_HTTP + Env.MAIN_SERVER_IP + LazyWebURI.URI_GUARDIAN();
            String uri_protected = URI.PREFIX_HTTP + Env.MAIN_SERVER_IP + LazyWebURI.URI_PROTECTED();
            String method = HttpConnection.GET;

            @Override
            public HttpResponse[] call() throws Exception {
                return new HttpResponse[]{
                        sendHttpRequest(uri_guardian, new JSONObject(), new JSONObject(), method),
                        sendHttpRequest(uri_protected, new JSONObject(), new JSONObject(), method)
                };
            }
        };

        Future<HttpResponse[]> future = executor.submit(guardianRequestTask);
        getGuardiansResponse = future.get()[0];
        getProtectedsResponse = future.get()[1];


        JSONObject guardiansRespBody = new JSONObject(new String(getGuardiansResponse.getBody()));
        JSONObject protectedsRespBody = new JSONObject(new String(getProtectedsResponse.getBody()));
        JSONArray guardiansJsonArray = (JSONArray) guardiansRespBody.get("guardians");
        JSONArray protectedsJsonArray = (JSONArray) protectedsRespBody.get("protecteds");

        Peers peerGroups = new Peers();

        if (guardiansJsonArray.length() > 0) {
            User[] guardians;
            guardians = new User[guardiansJsonArray.length()];
            for (int i = 0; i < guardiansJsonArray.length(); i++) {
                guardians[i] = new User();
                guardians[i].registerPeerUser(guardiansJsonArray.getJSONObject(i));
            }
            peerGroups.setGuardians(guardians);
        }

        if (protectedsJsonArray.length() > 0) {
            User[] protecteds;
            protecteds = new User[protectedsJsonArray.length()];
            for (int i = 0; i < protectedsJsonArray.length(); i++) {
                protecteds[i] = new User();
                protecteds[i].registerPeerUser(protectedsJsonArray.getJSONObject(i));
            }
            peerGroups.setProtecteds(protecteds);
        }

        return peerGroups;
    }


    public JSONObject getClientProfile() throws ExecutionException, InterruptedException {
        Callable<JSONObject> task = () -> {
            String uri = URI.PREFIX_HTTP + Env.MAIN_SERVER_IP + LazyWebURI.URI_SELF_PROFILE();
            try {
                HttpResponse result = sendHttpRequest(uri, new JSONObject(), new JSONObject(), HttpConnection.GET);
                String body = new String(result.getBody());
                JSONObject jsonData = new JSONObject(body);
                return jsonData;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                throw e;
            }
        };

        Future<JSONObject> future = executor.submit(task);
        return future.get();
    }


    public String getMyJWT() throws ExecutionException, InterruptedException {
        Callable<String> task = () -> {
            String uri = URI.PREFIX_HTTP + Env.MAIN_SERVER_IP + LazyWebURI.URI_MY_TOKEN();
            try {
                HttpResponse result = sendHttpRequest(uri, new JSONObject(), new JSONObject(), HttpConnection.GET);
                JSONObject resp = new JSONObject(new String(result.getBody()));
                String token = resp.getString("token");
                return token;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                throw e;
            }
        };

        Future<String> future = executor.submit(task);
        return future.get();
    }
}