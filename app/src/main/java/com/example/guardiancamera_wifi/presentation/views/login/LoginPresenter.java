package com.example.guardiancamera_wifi.presentation.views.login;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.example.guardiancamera_wifi.Env;
import com.example.guardiancamera_wifi.R;
import com.example.guardiancamera_wifi.data.api.http.MainServerConnection;
import com.example.guardiancamera_wifi.MyApplication;
import com.example.guardiancamera_wifi.domain.libs.types.Types;
import com.example.guardiancamera_wifi.domain.usecases.LoginUseCase;
import com.example.guardiancamera_wifi.presentation.views.app.MainMenuActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.kakao.auth.ApiResponseCallback;
import com.kakao.auth.AuthService;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.auth.network.response.AccessTokenInfoResponse;
import com.kakao.network.ErrorResult;
import com.kakao.util.exception.KakaoException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONException;


public class LoginPresenter {

    private Context applicationContext;
    private Activity activity;
    private LoginUseCase loginUseCase;

    /* Google client object & Intent request code */
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_GOOGLE_SIGN_IN = 7;
    private Intent mainMenuIntent;


    /**
     * Constructor.
     * Accepts necessary contexts from parent view (Activity).
     *
     * @param applicationContext
     * @param activity
     */
    public LoginPresenter(Context applicationContext, Activity activity) {
        this.applicationContext = applicationContext;
        this.activity = activity;
        this.mainMenuIntent = new Intent(activity, MainMenuActivity.class);
        this.loginUseCase = new LoginUseCase();
    }


    public void init() {
        initOAuthModules();
    }


    /**
     * Initialize OAuth2 Clients
     */
    public void initOAuthModules() {
        Session.getCurrentSession().addCallback(this.sessionCallback);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Env.GoogleClientID)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this.activity, gso);
    }


    /**
     * @param authProvider
     * @throws IOException
     */
    private void createBackendSession(Types.OAuthProvider authProvider) throws IOException {
        MyApplication.mainServerConn = new MainServerConnection(applicationContext,
                authProvider);
        MyApplication.mainServerConn.registerOAuthAccount();
    }


    /**
     * Kakao session callback function.
     * Registers user to application and move to next activity on success.
     * Prints error message to log on fail
     */
    private ISessionCallback sessionCallback = new ISessionCallback() {
        @Override
        public void onSessionOpened() {
            Log.i("KAKAO_SESSION", "로그인 성공");

            AuthService.getInstance()
                    .requestAccessTokenInfo(new ApiResponseCallback<AccessTokenInfoResponse>() {
                        @Override
                        public void onSessionClosed(ErrorResult errorResult) {
                            Log.e("KAKAO_API", "세션이 닫혀 있음: " + errorResult);
                        }

                        @Override
                        public void onFailure(ErrorResult errorResult) {
                            Log.e("KAKAO_API", "토큰 정보 요청 실패: " + errorResult);
                        }

                        @Override
                        public void onSuccess(AccessTokenInfoResponse result) {
                            Log.i("KAKAO_API", "사용자 아이디: " + result.toString());
                            Log.i("KAKAO_API", "남은 시간 (ms): " + result.getExpiresInMillis());
                        }
                    });


            try {
                createBackendSession(Types.OAuthProvider.AUTHENTICATOR_KAKAO);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            Intent intent = new Intent(applicationContext, MainMenuActivity.class);
            intent.putExtra(
                    applicationContext.getResources().getString(R.string.INDEX_LOGIN_METHOD),
                    applicationContext.getResources().getString(R.string.LOGIN_KAKAO)
            );

            activity.startActivity(intent);
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Log.e("KAKAO_SESSION", "로그인 실패", exception);
        }
    };


    /**
     * Begin Google OAuth procedure
     */
    public void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }


    /**
     * Notify result of Google OAuth attempt to caller.
     *
     * @return Success/Failure
     */
    private boolean handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            return !(account == null);
        } catch (ApiException e) {
            Log.w("Google Login Message", "signInResult:failed code=" + e.getStatusCode());
            return false;
        }
    }


    /**
     * Handle results from login attempt.
     * If login attempt was successful, start Main Menu activity.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void handleActivityResult(int requestCode, int resultCode, @Nullable Intent data)
            throws InterruptedException, ExecutionException, JSONException, IOException {
        Intent intent = new Intent(activity, MainMenuActivity.class);

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (handleGoogleSignInResult(task)) {
                intent.putExtra(
                        applicationContext.getResources().getString(R.string.INDEX_LOGIN_METHOD),
                        applicationContext.getResources().getString(R.string.LOGIN_GOOGLE)
                );
            } else {
                return;
            }


            try {
                createBackendSession(Types.OAuthProvider.AUTHENTICATOR_GOOGLE);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else {
            /* Invalid request */
            //@todo
        }

        MyApplication.mainServerConn.login(null, null);
        activity.startActivity(intent);
    }


    public void mainServerAuth(String username, String password) throws IOException {
        /* Application-scope backend connection manager */
        createBackendSession(Types.OAuthProvider.AUTHENTICATOR_NONSOCIAL);
        final MainServerConnection conn = MyApplication.mainServerConn;

        try {
            conn.pingServer();
            boolean success = loginUseCase.login(MyApplication.mainServerConn,
                    Types.OAuthProvider.AUTHENTICATOR_NONSOCIAL, username, password);

            /* Force delete credential */
            username = null;
            password = null;

            if (success) {
                conn.getUser();
                activity.startActivity(this.mainMenuIntent);
            }
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }
    }


    public void destroy() {
        Session.getCurrentSession().removeCallback(sessionCallback);
    }
}