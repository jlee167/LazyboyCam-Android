package com.example.guardiancamera_wifi.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.guardiancamera_wifi.R;
import com.example.guardiancamera_wifi.models.MyApplication;
import com.example.guardiancamera_wifi.presenters.LoginPresenter;
import com.google.android.gms.common.SignInButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;


public class LoginActivity extends AppCompatActivity {

    LoginPresenter loginPresenter;      // Presenter of this view


    /**
     * Initialize social login environment.
     * Create Google sign-in object and add Kakao session callback.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginPresenter = new LoginPresenter(getApplicationContext(), this);
        loginPresenter.init();
    }


    /**
     * Initialize Google Signin button with Google authentication event.
     * Kakao button already contains onClick method without any initialization.
     */
    @Override
    protected void onStart() {
        super.onStart();

        /**
         *  Google-provided Signin Button. Currently using custom button instead.
         */
//            final SignInButton signInButton = findViewById(R.id.sign_in_button);
//            signInButton.setSize(SignInButton.SIZE_WIDE);
//
//            signInButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    loginPresenter.googleSignIn();
//                }
//            });

        findViewById(R.id.GoogleSignBtnAlt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginPresenter.googleSignIn();
            }
        });

        findViewById(R.id.KakaoSignBtnAlt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.login_button_kakao).performClick();
            }
        });

        findViewById(R.id.login_button_kakao).setVisibility(View.GONE);

        findViewById(R.id.loginBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextInputEditText accountInput = findViewById(R.id.accountInput);
                TextInputEditText passwordInput = findViewById(R.id.passwordInput);
                try {
                    loginPresenter.mainServerAuth(accountInput.getText().toString(), passwordInput.getText().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

        /* Close Login Sessions */
        loginPresenter.destroy();
    }
}