package com.example.finalproject.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.model.Account;
import com.example.finalproject.model.HabitAction;
import com.example.finalproject.model.Habit;
import com.example.finalproject.model.HabitDatabaseHelper;
import com.example.finalproject.ui.MyPagerProgressAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ProgressActivity extends AppCompatActivity {
    private HabitDatabaseHelper databaseHelper;
    private Account acc;
    private int[] perfectArr = new int[32];
    private String idHabit, idTaiKhoan;
    
    TextView txtDIM, txtTTD, txtVTT, txtCS, tvDetail;
    ImageButton ibHome, ibMusic, ibClock, ibSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        databaseHelper = HabitDatabaseHelper.getInstance(this);

        // Nhận dữ liệu
        idHabit = getIntent().getStringExtra("idThoiQuen");
        idTaiKhoan = getIntent().getStringExtra("idTaiKhoan");
        acc = (Account) getIntent().getSerializableExtra("user_account");

        if (idHabit == null || idTaiKhoan == null) {
            Toast.makeText(this, "Lỗi: Thiếu dữ liệu thói quen", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initWidgets();
        setupViewPager();
        getDetailHabit();
    }

    private void initWidgets() {
        tvDetail = findViewById(R.id.tvDetail);
        txtDIM = findViewById(R.id.tvDoneinMonth);
        txtTTD = findViewById(R.id.tvTotalDone);
        txtVTT = findViewById(R.id.tvVolTotal);
        txtCS = findViewById(R.id.tvCurrentStreak);

        findViewById(R.id.btnDelete).setOnClickListener(v -> showDeleteDialog());
        findViewById(R.id.btnInfor).setOnClickListener(v -> showInforDialog());
        findViewById(R.id.btnEdit).setOnClickListener(v -> {
            Intent i = new Intent(this, Edit_habit.class);
            i.putExtra("idTaiKhoan", idTaiKhoan);
            i.putExtra("idThoiQuen", idHabit);
            i.putExtra("user_account", acc);
            startActivity(i);
        });

        // Bottom Navigation
        findViewById(R.id.ib_home).setOnClickListener(v -> navigateTo(Home_Activity.class));
        findViewById(R.id.ib_music).setOnClickListener(v -> navigateTo(SongsActivity.class));
        findViewById(R.id.ib_clock).setOnClickListener(v -> navigateTo(Pomorodo.class));
        findViewById(R.id.ib_settings).setOnClickListener(v -> navigateTo(Setting.class));
    }

    private void navigateTo(Class<?> cls) {
        Intent i = new Intent(this, cls);
        i.putExtra("idTaiKhoan", idTaiKhoan);
        i.putExtra("user_account", acc);
        startActivity(i);
    }

    private void setupViewPager() {
        TabLayout tabLayout = findViewById(R.id.tabLayoutProgress);
        ViewPager viewPager = findViewById(R.id.viewpagerProgress);
        MyPagerProgressAdapter adapter = new MyPagerProgressAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), idHabit, idTaiKhoan, "");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void getDetailHabit() {
        try {
            Habit habit = databaseHelper.getHabit(idTaiKhoan, idHabit);
            if (habit == null) return;

            Arrays.fill(perfectArr, 0);
            List<HabitAction> actions = databaseHelper.getHabitActions(idTaiKhoan, idHabit);
            for (HabitAction action : actions) {
                if (databaseHelper.isInCurrentMonth(action.getActionTime())) {
                    Calendar calendar = databaseHelper.parseActionTime(action.getActionTime());
                    if (calendar != null) perfectArr[calendar.get(Calendar.DAY_OF_MONTH)] = 1;
                }
            }

            tvDetail.setText(habit.getTen());
            txtTTD.setText(databaseHelper.getTotalDaysWithProgress(idTaiKhoan, idHabit) + " Days");
            txtVTT.setText(String.format(Locale.getDefault(), "%.1f %s", databaseHelper.getTotalProgress(idTaiKhoan, idHabit), habit.getDonVi()));
            txtDIM.setText(databaseHelper.getDoneInCurrentMonth(idTaiKhoan, idHabit) + " Days");
            txtCS.setText(calculateStreak() + " Days");
        } catch (Exception e) {
            Log.e("ProgressActivity", "Error", e);
        }
    }

    private int calculateStreak() {
        int max = 0, current = 0;
        for (int val : perfectArr) {
            if (val == 1) {
                current++;
                max = Math.max(max, current);
            } else {
                current = 0;
            }
        }
        return max;
    }

    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_delete, null); // Đã sửa: R.layout thay vì R.drawable
        builder.setView(view);

        AlertDialog dialog = builder.create();
        
        // Cấu hình nút trong dialog nếu bạn dùng nút mặc định của Builder
        builder.setPositiveButton("Delete", (d, w) -> {
            databaseHelper.deleteHabit(idTaiKhoan, idHabit);
            Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show();
            navigateTo(Home_Activity.class);
            finish();
        });
        builder.setNegativeButton("Cancel", (d, w) -> d.dismiss());
        builder.show();
    }

    private void showInforDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_infor);
        
        Habit h = databaseHelper.getHabit(idTaiKhoan, idHabit);
        if (h != null) {
            ((EditText)dialog.findViewById(R.id.edt_name)).setText(h.getTen());
            ((TextInputEditText)dialog.findViewById(R.id.edt_decription)).setText(h.getMoTa());
            ((EditText)dialog.findViewById(R.id.edt_goal)).setText(String.valueOf((int)h.getMucTieu()));
            ((EditText)dialog.findViewById(R.id.edt_unit)).setText(h.getDonVi());
        }

        dialog.findViewById(R.id.img_close).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }
}
