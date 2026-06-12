package com.example.finalproject.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.model.Account;
import com.example.finalproject.model.HabitDatabaseHelper;
import com.google.android.material.button.MaterialButton;

public class WelcomeActivity extends AppCompatActivity {
    private HabitDatabaseHelper databaseHelper;
    private static final String PREF_NAME = "login_prefs";
    private static final String KEY_USERNAME = "remember_username";
    private static final String KEY_PASSWORD = "remember_password";
    private static final String KEY_REMEMBER_TIME = "remember_time";
    private static final long ONE_MONTH_MS = 30L * 24 * 60 * 60 * 1000;

    MaterialButton btnLogin;
    TextView btnSignup, btnForgotPassword;
    EditText etUsername, etPassword;
    TextView tvWelcomeTitle;
    CheckBox cbRememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ẩn ActionBar để background lên sát đỉnh
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_welcome);

        databaseHelper = HabitDatabaseHelper.getInstance(this);

        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);
        btnForgotPassword = findViewById(R.id.btnForgot);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        tvWelcomeTitle = findViewById(R.id.tvWelcomeTitle);
        cbRememberMe = findViewById(R.id.cbRememberMe);

        checkRememberedLogin();

        btnSignup.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
        btnForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotActivity.class)));
        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void checkRememberedLogin() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String savedUser = prefs.getString(KEY_USERNAME, null);
        String savedPass = prefs.getString(KEY_PASSWORD, null);
        long savedTime = prefs.getLong(KEY_REMEMBER_TIME, 0);

        if (savedUser != null && savedPass != null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - savedTime < ONE_MONTH_MS) {
                etUsername.setText(savedUser);
                etPassword.setText(savedPass);
                autoLogin(savedUser, savedPass);
            } else {
                clearRememberedLogin();
            }
        }
    }

    private void autoLogin(String u, String p) {
        Account account = databaseHelper.getAccountByUsernameAndPassword(u, p);
        String id = databaseHelper.getAccountIdByUsernameAndPassword(u, p);
        if (account != null && id != null) {
            goToHome(account, id);
        }
    }

    private void loginUser() {
        String u = etUsername.getText().toString().trim();
        String p = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(u) || TextUtils.isEmpty(p)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Account account = databaseHelper.getAccountByUsernameAndPassword(u, p);
        String id = databaseHelper.getAccountIdByUsernameAndPassword(u, p);

        if (account != null && id != null) {
            if (cbRememberMe.isChecked()) {
                saveLoginInfo(u, p);
            } else {
                clearRememberedLogin();
            }
            goToHome(account, id);
        } else {
            Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveLoginInfo(String u, String p) {
        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.putString(KEY_USERNAME, u);
        editor.putString(KEY_PASSWORD, p);
        editor.putLong(KEY_REMEMBER_TIME, System.currentTimeMillis());
        editor.apply();
    }

    private void clearRememberedLogin() {
        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_PASSWORD);
        editor.remove(KEY_REMEMBER_TIME);
        editor.apply();
    }

    private void goToHome(Account account, String id) {
        Intent intent = new Intent(this, Home_Activity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user_account", account);
        intent.putExtra("idTaiKhoan", id);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }
}
