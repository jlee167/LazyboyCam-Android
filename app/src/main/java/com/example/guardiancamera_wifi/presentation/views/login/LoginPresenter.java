package com.example.guardiancamera_wifi.presentation.views.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.guardiancamera_wifi.Env;
import com.example.guardiancamera_wifi.MyApplication;
import com.example.guardiancamera_wifi.data.repository.user.UserRepositoryImpl;
import com.example.guardiancamera_wifi.domain.model.Types;
import com.example.guardiancamera_wifi.domain.repository.user.UserRepository;
import com.example.guardiancamera_wifi.domain.usecase.jwt.GetMyJwtUseCase;
import com.example.guardiancamera_wifi.domain.usecase.login.LoginRequest;
import com.example.guardiancamera_wifi.domain.usecase.login.LoginUseCase;
import com.example.guardiancamera_wifi.domain.usecase.login.exceptions.InvalidCredentialException;
import com.example.guardiancamera_wifi.domain.usecase.peers.GetPeersUseCase;
import com.example.guardiancamera_wifi.domain.usecase.user.GetUserProfileUseCase;
import com.example.guardiancamera_wifi.domain.usecase.user.exceptions.UserNotFoundException;
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

import org.json.JSONException;

import java.util.concurrent.ExecutionException;


public class LoginPresenter {

    private Activity activity;
    private Context applicationContext;

    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_GOOGLE_SIGN_IN = 7;
    private Intent mainMenuIntent;

    private UserRepository userRepository;
    private LoginUseCase loginUseCase;
    private GetUserProfileUseCase getUserProfileUseCase;
    private GetPeersUseCase getPeersUseCase;
    private GetMyJwtUseCase getMyJwtUseCase;


    public LoginPresenter(Context applicationContext, Activity activity) {
        this.applicationContext = applicationContext;
        this.activity = activity;
        this.mainMenuIntent = new Intent(activity, MainMenuActivity.class);

        userRepository = new UserRepositoryImpl();
        getMyJwtUseCase = new GetMyJwtUseCase(userRepository);
        loginUseCase = new LoginUseCase(userRepository);
        getUserProfileUseCase = new GetUserProfileUseCase(userRepository);
        getPeersUseCase = new GetPeersUseCase(userRepository);
    }


    public void initOAuthModules() {
        Session.getCurrentSession().addCallback(this.sessionCallback);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Env.GOOGLE_CLIENT_ID)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this.activity, gso);
    }


    /**
     * Kakao session callback function.
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

            LoginRequest request = getKakaoUserLoginRequest();
            login(request);
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Log.e("KAKAO_SESSION", "로그인 실패", exception);
        }
    };


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
     * Get user's Google OAuth account and attempt authentication with
     * authentication server
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void handleActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (handleGoogleSignInResult(task)) {
                LoginRequest request = getGoogleUserLoginRequest();
                login(request);
            }
        }
    }


    private LoginRequest getKakaoUserLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setMainServerConn(MyApplication.mainServerConn);
        request.setAuthProvider(Types.OAuthProvider.AUTHENTICATOR_KAKAO);
        request.setOAuthAccessToken(Session.getCurrentSession().getTokenInfo().getAccessToken());
        request.setUsername(null);
        request.setPassword(null);
        return request;
    }


    private LoginRequest getGoogleUserLoginRequest() {
        GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(applicationContext);
        assert googleAccount != null;

        LoginRequest request = new LoginRequest();
        request.setMainServerConn(MyApplication.mainServerConn);
        request.setAuthProvider(Types.OAuthProvider.AUTHENTICATOR_GOOGLE);
        request.setOAuthAccessToken(googleAccount.getIdToken());
        return request;
    }


    public LoginRequest getNonSocialLoginRequest(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setMainServerConn(MyApplication.mainServerConn);
        request.setAuthProvider(Types.OAuthProvider.AUTHENTICATOR_NONSOCIAL);
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }


    public void login(LoginRequest request) {
        try {
            authUser(request);
            updateUserData();
        } catch (Exception e) {
            return;
        }

        onLoginUiUpdate();
    }


    private void onLoginUiUpdate() {
        activity.startActivity(this.mainMenuIntent);
    }


    private void authUser(LoginRequest request)
            throws InterruptedException, ExecutionException, InvalidCredentialException {
        loginUseCase.execute(request);
    }


    private void updateUserData() throws UserNotFoundException,
            ExecutionException, JSONException, InterruptedException {
        MyApplication.currentUser =  getUserProfileUseCase.execute();
        MyApplication.peers = getPeersUseCase.execute();
        MyApplication.currentUser.setStreamAccessToken(getMyJwtUseCase.execute());
    }


    public void destroy() {
        Session.getCurrentSession().removeCallback(sessionCallback);
    }
}