package com.example.finalproject.activities;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.finalproject.R;
import com.example.finalproject.model.Account;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class Setting extends AppCompatActivity {
    Handler mainHandler = new Handler();

    private Account getAccount = new Account();

    TextView txtName, txtGender, txtBorn, txtPhone, txtGmail;
    ImageButton imgBtnClcok, imgBtnMusic, imgBtnGraph, imgBtnHome;
    Button btnEdit;
    MaterialButton btnLogout;
    ImageView imageView;
    MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        doFormWidget();
        getData();
    }
    public void doFormWidget(){
        String idTaiKhoan = getIntent().getStringExtra("idTaiKhoan");
        txtName = findViewById(R.id.txtname);
        txtGender = findViewById(R.id.txtgender);
        txtBorn = findViewById(R.id.txtBirthday);
        txtPhone = findViewById(R.id.txtPhone);
        txtGmail = findViewById(R.id.txtgmail);
        imageView = findViewById(R.id.imvAvatar);
        btnEdit = findViewById(R.id.btnEdit);
        btnLogout = findViewById(R.id.btnLogout);
        imgBtnGraph = findViewById(R.id.ib_graph);
        imgBtnClcok = findViewById(R.id.ib_clock);
        imgBtnHome = findViewById(R.id.ib_home);
        imgBtnMusic = findViewById(R.id.ib_music);
        toolbar = findViewById(R.id.toolbar);

        imgBtnGraph.setOnClickListener(v -> {
            Intent intent = new Intent(Setting.this, Progress_total.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("user_account", getAccount);
            intent.putExtra("idTaiKhoan", idTaiKhoan);
            intent.putExtras(bundle);
            startActivity(intent);
        });
        imgBtnClcok.setOnClickListener(v -> {
            Intent intent = new Intent(Setting.this, Pomorodo.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("user_account", getAccount);
            intent.putExtra("idTaiKhoan", idTaiKhoan);
            intent.putExtras(bundle);
            startActivity(intent);
        });
        imgBtnHome.setOnClickListener(v -> {
            Intent intent = new Intent(Setting.this, Home_Activity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("user_account", getAccount);
            intent.putExtra("idTaiKhoan", idTaiKhoan);
            intent.putExtras(bundle);
            startActivity(intent);
        });
        imgBtnMusic.setOnClickListener(v -> {
            Intent intent = new Intent(Setting.this, SongsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("user_account", getAccount);
            intent.putExtra("idTaiKhoan", idTaiKhoan);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Setting.this, SettingEditInfor.class );
                Bundle bundle = new Bundle();
                bundle.putSerializable("user_account", getAccount);
                i.putExtra("idTaiKhoan", idTaiKhoan);
                i.putExtras(bundle);
                startActivity(i);
            }
        });

        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        // Xóa thông tin đăng nhập đã lưu trong SharedPreferences
        SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("remember_username");
        editor.remove("remember_password");
        editor.remove("remember_time");
        editor.apply();

        // Chuyển về màn hình Welcome (đăng nhập)
        Intent intent = new Intent(Setting.this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void getData(){
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Account account = (Account) bundle.getSerializable("user_account");
            if (account != null) {
                getAccount = account;
                txtName.setText(account.getName());
                txtGender.setText(account.getSex());
                txtPhone.setText(account.getPhone());
                txtGmail.setText(account.getGmail());
                txtBorn.setText(account.getBorn());
                Glide.with(this).load(account.getAvatar()).into(imageView);
            }
        }

    }
}
