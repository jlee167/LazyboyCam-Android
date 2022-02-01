package com.example.guardiancamera_wifi.presentation.views.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.guardiancamera_wifi.R;
import com.example.guardiancamera_wifi.domain.usecase.login.LoginRequest;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;


public class LoginActivity extends AppCompatActivity {

    private LoginPresenter loginPresenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginPresenter = new LoginPresenter(getApplicationContext(), this);
        loginPresenter.initOAuthModules();
    }


    @Override
    protected void onStart() {
        super.onStart();
        initAuthUI();
    }


    private void initAuthUI() {
        findViewById(R.id.GoogleSignBtnAlt).setOnClickListener(view -> loginPresenter.googleSignIn());

        /* Use custom-made button for Kakao UI */
        findViewById(R.id.KakaoSignBtnAlt).setOnClickListener(view -> findViewById(R.id.login_button_kakao).performClick());
        findViewById(R.id.login_button_kakao).setVisibility(View.GONE);

        findViewById(R.id.loginBtn).setOnClickListener(view -> {
            TextInputEditText accountInput = findViewById(R.id.accountInput);
            String account = Objects.requireNonNull(accountInput.getText()).toString();

            TextInputEditText passwordInput = findViewById(R.id.passwordInput);
            String password = Objects.requireNonNull(passwordInput.getText()).toString();

            LoginRequest request = loginPresenter.getNonSocialLoginRequest(account, password);
            loginPresenter.login(request);
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginPresenter.handleActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        loginPresenter.destroy();
    }
}
