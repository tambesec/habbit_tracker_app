package com.example.finalproject.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.model.Account;
import com.example.finalproject.model.HabitDatabaseHelper;
import com.example.finalproject.model.GmailSender;

public class ForgotActivity extends AppCompatActivity {

    private HabitDatabaseHelper databaseHelper;
    private Button btnGetPass;
    private ImageButton imgLogin;
    private GmailSender gmailSender = new GmailSender();
    private EditText edt_username, edt_email;
    private TextView tvWelcomeTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

        databaseHelper = HabitDatabaseHelper.getInstance(this);

        imgLogin = findViewById(R.id.imgLogin_Forgot);
        imgLogin.setOnClickListener(v -> finish());

        tvWelcomeTitle = findViewById(R.id.tvWelcomeTitle);
        Animation animation_tv = AnimationUtils.loadAnimation(this, R.anim.anime_tv);
        tvWelcomeTitle.setAnimation(animation_tv);

        edt_username = findViewById(R.id.edt_username);
        edt_email = findViewById(R.id.edt_email);
        Animation animation_et = AnimationUtils.loadAnimation(this, R.anim.anime_et_left);
        edt_username.setAnimation(animation_et);
        edt_email.setAnimation(animation_et);

        btnGetPass = findViewById(R.id.btnGetpass);
        btnGetPass.setOnClickListener(v -> {
            String username = edt_username.getText().toString().trim();
            String gmail = edt_email.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(gmail)) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Truy vấn an toàn
            String accountId = databaseHelper.getAccountIdByUsernameAndGmail(username, gmail);
            if (accountId == null) {
                Toast.makeText(this, "Thông tin không chính xác hoặc tài khoản không tồn tại", Toast.LENGTH_SHORT).show();
                return;
            }

            Account account = databaseHelper.getAccountById(accountId);
            if (account == null) {
                Toast.makeText(this, "Lỗi: Không thể lấy dữ liệu tài khoản", Toast.LENGTH_SHORT).show();
                return;
            }

            sendPasswordEmail(account, gmail);
        });
    }

    private void sendPasswordEmail(Account account, String recipientEmail) {
        String currentPassword = account.getPassword();
        String senderEmail = "01215165330asd@gmail.com";
        String senderPassword = "uwcpdnjcsxhchtcv"; // App Password 16 ký tự của Gmail
        
        String subject = "Habit Tracker - Khôi phục mật khẩu";
        String messageBody = "Xin chào " + account.getName() + ",\n\nMật khẩu đăng nhập của bạn là: " + currentPassword;

        try {
            gmailSender.sendEmail(senderEmail, senderPassword, recipientEmail, subject, messageBody);
            Toast.makeText(this, "Mật khẩu đã được gửi vào Email của bạn", Toast.LENGTH_LONG).show();
            
            // Quay lại màn hình đăng nhập sau khi gửi mail thành công
            Intent intent = new Intent(ForgotActivity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi gửi mail: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
