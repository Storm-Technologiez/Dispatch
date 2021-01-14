package com.example.dispatch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {
    final String MY_PREFS_NAME = "details";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!RestoreData()) {
            startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
            finish();
        }

        setContentView(R.layout.activity_login);
    }

    public void GotoLogin(View view) {
        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
        finish();
    }

    public void GotoSignUp(View view) {
        startActivity(new Intent(WelcomeActivity.this, SignUpActivity.class));
        finish();
    }

    private boolean RestoreData() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        Boolean isFirstTimeUser = prefs.getBoolean("firstTimeUser", true);
        return isFirstTimeUser;
    }
}