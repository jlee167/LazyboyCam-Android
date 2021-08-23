package com.example.guardiancamera_wifi.data.api.http;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.example.guardiancamera_wifi.data.configs.Addresses;
import com.example.guardiancamera_wifi.data.configs.LazyWebURI;
import com.example.guardiancamera_wifi.data.api.http.base.HttpConnection;
import com.example.guardiancamera_wifi.domain.models.LazyWebPeers;
import com.example.guardiancamera_wifi.domain.models.LazyWebUser;
import com.example.guardiancamera_wifi.domain.libs.types.Types;
import com.example.guardiancamera_wifi.MyApplication;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.Profile;
import com.kakao.usermgmt.response.model.UserAccount;
import com.kakao.util.OptionalBoolean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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

    private Types.OAuthProvider authProvider;

    /* Caller activity context & passed intent */
    private Context appContext;

    /* Login Information */
    private GoogleSignInAccount googleAccount;
    private UserAccount kakaoAccount;

    private BufferedOutputStream outputStream;
    private BufferedInputStream inputStream;

    private static boolean session_enabled = false;
    private static String COOKIES = "";
    private CookieManager cookieManager = new CookieManager();

    private ExecutorService executor = Executors.newFixedThreadPool(2);

    /**
     * Default Constructor.
     * Get the owner's environments and copy to the local variables.
     *
     * @param applicationContext Current application's context
     * @throws MalformedURLException
     */
    public MainServerConnection(final Context applicationContext, final Types.OAuthProvider auth)
            throws IOException {
        appContext = applicationContext;
        authProvider = auth;
        registerOAuthAccount();
    }


    public Types.OAuthProvider getAuthProvider() {
        return this.authProvider;
    }

    public void setAuthProvider(Types.OAuthProvider authProvider) {
        this.authProvider = authProvider;
    }


    public String getKakaoAccessToken() {
        return Session.getCurrentSession().getTokenInfo().getAccessToken();
    }

    public String getGoogleAccessToken() {
        return googleAccount.getIdToken();
    }


    /**
     * Get user's social login account
     * Store account instance to either 'googleAccount' or 'kakaoAccount' variable
     *
     * @return True if signed in with Kakao or Google
     */
    public void registerOAuthAccount() {

        switch (this.authProvider) {
            case AUTHENTICATOR_GOOGLE: {
                googleAccount = GoogleSignIn.getLastSignedInAccount(appContext);
                break;
            }

            case AUTHENTICATOR_KAKAO: {
                UserManagement.getInstance().me(new MeV2ResponseCallback() {
                    @Override
                    public void onSessionClosed(ErrorResult errorResult) {
                        Log.e("KAKAO_API", "세션이 닫혀 있음: " + errorResult);
                    }

                    @Override
                    public void onFailure(ErrorResult errorResult) {
                        Log.e("KAKAO_API", "사용자 정보 요청 실패: " + errorResult);
                    }

                    @Override
                    public void onSuccess(MeV2Response result) {
                        Log.i("KAKAO_API", "사용자 아이디: " + result.getId());

                        kakaoAccount = result.getKakaoAccount();
                        if (kakaoAccount != null) {
                            // 이메일
                            String email = kakaoAccount.getEmail();

                            if (email != null) {
                                Log.i("KAKAO_API", "email: " + email);

                            } else if (kakaoAccount.emailNeedsAgreement() == OptionalBoolean.TRUE) {
                                // 동의 요청 후 이메일 획득 가능
                                // 단, 선택 동의로 설정되어 있다면 서비스 이용 시나리오 상에서 반드시 필요한 경우에만 요청해야 합니다.

                            } else {
                                // 이메일 획득 불가
                            }

                            // 프로필
                            Profile profile = kakaoAccount.getProfile();

                            if (profile != null) {
                                Log.d("KAKAO_API", "nickname: " + profile.getNickname());
                                Log.d("KAKAO_API", "profile image: " + profile.getProfileImageUrl());
                                Log.d("KAKAO_API", "thumbnail image: " + profile.getThumbnailImageUrl());

                            } else if (kakaoAccount.profileNeedsAgreement() == OptionalBoolean.TRUE) {
                                // 동의 요청 후 프로필 정보 획득 가능

                            } else {
                                // 프로필 획득 불가
                            }
                        }
                    }
                });

                break;
            }

            default:
                return;
        }
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
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        pingThread.start();
        pingThread.join();
    }


    public JSONObject getCredential(String username, String password)
            throws JSONException {
        JSONObject authCredential = new JSONObject();

        switch (this.authProvider) {
            case AUTHENTICATOR_GOOGLE: {
                authCredential.put("accessToken", getGoogleAccessToken());
                break;
            }

            case AUTHENTICATOR_KAKAO: {
                authCredential.put("accessToken", getKakaoAccessToken());
                break;
            }

            default: {
                authCredential.put("username", username);
                authCredential.put("password", password);
                break;
            }
        }

        return authCredential;
    }


    /**
     * @param uri
     * @param tokens_json
     * @param method
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public String sendHttpRequest(String uri, JSONObject tokens_json, String method)
            throws IOException, JSONException {
        URL authServerUrl;
        HttpURLConnection httpConn;

        authServerUrl = new URL(Addresses.PREFIX_HTTP
                + Addresses.MAIN_SERVER_URL
                + uri);

        boolean outputEnabled = method.equals(HttpConnection.POST) || method.equals(HttpConnection.PUT);

        try {
            httpConn = (HttpURLConnection) authServerUrl.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if (session_enabled)
            httpConn.setRequestProperty("Cookie", COOKIES);

        httpConn.setRequestProperty("Content-Type", "application/json; utf-8");
        httpConn.setRequestProperty("Accept", "application/json");
        try {
            httpConn.setRequestMethod(method);
        } catch (ProtocolException e) {
            e.printStackTrace();
        }

        if (outputEnabled) {
            httpConn.setDoOutput(true);
        }

        httpConn.setDoInput(true);
        httpConn.connect();


        if (outputEnabled) {
            try {
                /* Http output stream */
                outputStream = new BufferedOutputStream(httpConn.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            //streamToAuthServer.write(tokens_json.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.write(tokens_json.toString().getBytes());
            outputStream.flush();
            outputStream.close();
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
                        URI.create(Addresses.PREFIX_HTTP
                                + Addresses.MAIN_SERVER_URL),
                        HttpCookie.parse(cookie.get(i)).get(0)
                );
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


    /**
     * Sign in to lazyweb
     *
     * @param username
     * @param password
     * @return Success(True) or Fail(False)
     */
    public Boolean login(final String username, final String password)
            throws ExecutionException, InterruptedException {

        Callable<Boolean> task = new Callable<Boolean>() {

            private Types.OAuthProvider mAuthProvider = authProvider;
            private String mUsername = username;
            private String mPassword = password;
            private String uri;
            private JSONObject result;

            /**
             * Computes a result, or throws an exception if unable to do so.
             *
             * @return computed result
             * @throws Exception if unable to compute a result
             */
            @Override
            public Boolean call() throws Exception {
                switch (this.mAuthProvider) {
                    case AUTHENTICATOR_KAKAO: {
                        uri = LazyWebURI.URI_LOGIN() + LazyWebURI.URI_KAKAO();
                        break;
                    }

                    case AUTHENTICATOR_GOOGLE: {
                        uri = LazyWebURI.URI_LOGIN() + LazyWebURI.URI_GOOGLE();
                        break;
                    }

                    default: {
                        uri = LazyWebURI.URI_LOGIN();
                    }
                }

                try {
                    result = new JSONObject(sendHttpRequest(
                            uri,
                            getCredential(mUsername, mPassword),
                            HttpConnection.POST
                    ));
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

                return true;
            }
        };

        Future<Boolean> future = executor.submit(task);
        return future.get();
    }


    /**
     * Connect to the authentication server (Http protocol)
     * Pass Google or Kakao authentication token to the server for validation.
     */
    public LazyWebPeers getPeers() throws JSONException, ExecutionException, InterruptedException {

        /*
        JSONArray guardiansQueryResult = sendHttpRequest(LazyWebURI.URI_GUARDIAN(),
                                                            new JSONObject(), HTTP.GET);
        JSONArray protectedsQueryResult = sendHttpRequest(LazyWebURI.URI_PROTECTED(),
                                                            new JSONObject(), HTTP.GET);
        */

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


        // User information objects to fill.
        // User info arrays will be used to fill the peer groups object, which will be returned
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





    public Boolean getUser() throws IOException, JSONException, ExecutionException, InterruptedException {

        Callable<Boolean> task = new Callable<Boolean>() {

            private Types.OAuthProvider mAuthProvider = authProvider;
            private String uri = LazyWebURI.URI_USER();
            private JSONObject result;

            @Override
            public Boolean call() throws Exception {
                try {
                    result = new JSONObject(sendHttpRequest(
                            uri,
                            new JSONObject(),
                            HttpConnection.POST
                    ));
                    MyApplication.currentUser = new LazyWebUser(result);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                return true;
            }
        };

        Future<Boolean> future = executor.submit(task);
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