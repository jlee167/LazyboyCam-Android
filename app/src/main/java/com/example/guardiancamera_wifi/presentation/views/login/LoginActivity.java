package com.example.guardiancamera_wifi.presentation.views.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.guardiancamera_wifi.R;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;


public class LoginActivity extends AppCompatActivity {

    LoginPresenter loginPresenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginPresenter = new LoginPresenter(getApplicationContext(), this);
        loginPresenter.init();
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
            TextInputEditText passwordInput = findViewById(R.id.passwordInput);
            try {
                loginPresenter.mainServerAuth(accountInput.getText().toString(), passwordInput.getText().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            loginPresenter.handleActivityResult(requestCode, resultCode, data);
        } catch (InterruptedException | ExecutionException | JSONException | IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        loginPresenter.destroy();
    }
}
