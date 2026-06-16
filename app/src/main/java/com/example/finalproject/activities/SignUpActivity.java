package com.example.finalproject.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.model.Account;
import com.example.finalproject.model.HabitDatabaseHelper;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class SignUpActivity extends AppCompatActivity {
    HabitDatabaseHelper databaseHelper;
    TextView tvWelcomeTitle, btnLogin_SignUp;
    MaterialButton btnSignup;
    EditText edtUsername, edtEmail, edtPassword, edtRepassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ẩn ActionBar để background lên sát đỉnh màn hình
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_sign_up);

        databaseHelper = HabitDatabaseHelper.getInstance(this);

        // Ánh xạ View
        btnSignup = findViewById(R.id.btnSignup);
        btnLogin_SignUp = findViewById(R.id.btnLogin_SignUp);
        tvWelcomeTitle = findViewById(R.id.tvWelcomeTitle);
        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtRepassword = findViewById(R.id.edtRepassword);

        // Thiết lập Animation
        Animation animation_tv = AnimationUtils.loadAnimation(this, R.anim.anime_tv);
        Animation animation_et_left = AnimationUtils.loadAnimation(this, R.anim.anime_et_left);
        Animation animation_et_right = AnimationUtils.loadAnimation(this, R.anim.anime_et_right);

        tvWelcomeTitle.setAnimation(animation_tv);
        edtUsername.setAnimation(animation_et_left);
        edtEmail.setAnimation(animation_et_right);
        edtPassword.setAnimation(animation_et_left);
        edtRepassword.setAnimation(animation_et_right);

        // Xử lý sự kiện
        btnSignup.setOnClickListener(v -> SignUp());
        
        btnLogin_SignUp.setOnClickListener(v -> {
            finish(); // Quay lại màn hình Login
        });
    }

    public void SignUp() {
        String username = edtUsername.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String rePassword = edtRepassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra định dạng email cơ bản
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Account> allAccounts = databaseHelper.getAllAccounts();

        // Kiểm tra username trùng lặp
        if (allAccounts.stream().anyMatch(acc -> username.equals(acc.getUsername()))) {
            Toast.makeText(this, "Tên người dùng đã tồn tại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra email trùng lặp
        if (allAccounts.stream().anyMatch(acc -> email.equalsIgnoreCase(acc.getGmail()))) {
            Toast.makeText(this, "Email đã được sử dụng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(rePassword)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        Account newAccount = new Account();
        newAccount.setUsername(username);
        newAccount.setPassword(password);
        newAccount.setAvatar("https://img.freepik.com/free-psd/3d-illustration-person-with-sunglasses_23-2149436188.jpg?w=1380");
        newAccount.setName("Người dùng mới");
        newAccount.setSex("Nam");
        newAccount.setBorn("01-01-2000");
        newAccount.setGmail(email);
        newAccount.setPhone("Chưa thiết lập");

        databaseHelper.insertAccount(newAccount);
        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
