package com.example.finalproject.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.button.MaterialButton;
import com.example.finalproject.R;

public class MainActivity extends AppCompatActivity {
    // Khai báo các nút bấm theo giao diện Material 3 mới
    MaterialButton btnWelcome, btnHomeMain, btnSettings, btnCreateHabit, btnProgress, btnSong, btnPomorodo, btnLaunchApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ ID từ activity_main.xml
        btnWelcome = findViewById(R.id.btnWelcome);
        btnHomeMain = findViewById(R.id.btnHomeMain);
        btnSettings = findViewById(R.id.btnSettings);
        btnCreateHabit = findViewById(R.id.btnCreateHabit);
        btnProgress = findViewById(R.id.btnProgress);
        btnSong = findViewById(R.id.btnSong);
        btnPomorodo = findViewById(R.id.btnPomorodo);
        btnLaunchApp = findViewById(R.id.button);

        // Thiết lập sự kiện chuyển màn hình
        btnWelcome.setOnClickListener(v -> startActivity(new Intent(this, WelcomeActivity.class)));
        btnHomeMain.setOnClickListener(v -> startActivity(new Intent(this, Home_Activity.class)));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, Setting.class)));
        btnCreateHabit.setOnClickListener(v -> startActivity(new Intent(this, Create_habit.class)));
        btnProgress.setOnClickListener(v -> startActivity(new Intent(this, ProgressActivity.class)));
        btnSong.setOnClickListener(v -> startActivity(new Intent(this, SongsActivity.class)));
        btnPomorodo.setOnClickListener(v -> startActivity(new Intent(this, Pomorodo.class)));
        btnLaunchApp.setOnClickListener(v -> startActivity(new Intent(this, WelcomeActivity.class)));
    }
}
