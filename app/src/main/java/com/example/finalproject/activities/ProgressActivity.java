package com.example.finalproject.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.model.Account;
import com.example.finalproject.model.HabitAction;
import com.example.finalproject.model.Habit;
import com.example.finalproject.model.HabitDatabaseHelper;
import com.example.finalproject.ui.MyPagerProgressAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProgressActivity extends AppCompatActivity {
    private HabitDatabaseHelper databaseHelper;
    private Account acc;
    private int[] perfectArr = new int[32];
    private String idHabit, idTaiKhoan;
    
    TextView txtDIM, txtTTD, txtVTT, txtCS, tvDetail;
    TextView tvHabitDesc, tvHabitSchedule, tvHabitTime, tvHabitDuration;
    ViewPager viewPager;
    MyPagerProgressAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        databaseHelper = HabitDatabaseHelper.getInstance(this);

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDetailHabit();
    }

    private void initWidgets() {
        tvDetail = findViewById(R.id.tvDetail);
        txtDIM = findViewById(R.id.tvDoneinMonth);
        txtTTD = findViewById(R.id.tvTotalDone);
        txtVTT = findViewById(R.id.tvVolTotal);
        txtCS = findViewById(R.id.tvCurrentStreak);
        
        tvHabitDesc = findViewById(R.id.tvHabitDesc);
        tvHabitSchedule = findViewById(R.id.tvHabitSchedule);
        tvHabitTime = findViewById(R.id.tvHabitTime);
        tvHabitDuration = findViewById(R.id.tvHabitDuration);

        findViewById(R.id.btnDelete).setOnClickListener(v -> showDeleteDialog());
        findViewById(R.id.btnInfor).setOnClickListener(v -> showInforDialog());
        findViewById(R.id.btnEdit).setOnClickListener(v -> {
            Intent i = new Intent(this, Edit_habit.class);
            i.putExtra("idTaiKhoan", idTaiKhoan);
            i.putExtra("idThoiQuen", idHabit);
            i.putExtra("user_account", acc);
            startActivity(i);
        });

        findViewById(R.id.ib_home).setOnClickListener(v -> navigateTo(Home_Activity.class));
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
        viewPager = findViewById(R.id.viewpagerProgress);
        adapter = new MyPagerProgressAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), idHabit, idTaiKhoan, "");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void getDetailHabit() {
        try {
            Habit habit = databaseHelper.getHabit(idTaiKhoan, idHabit);
            if (habit == null) return;

            Arrays.fill(perfectArr, 0);
            List<HabitAction> actions = databaseHelper.getHabitActions(idTaiKhoan, idHabit);
            
            Map<Integer, Double> dailySums = new HashMap<>();
            double target = habit.getMucTieu();

            for (HabitAction action : actions) {
                if (databaseHelper.isInCurrentMonth(action.getActionTime())) {
                    Calendar calendar = databaseHelper.parseActionTime(action.getActionTime());
                    if (calendar != null) {
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        dailySums.put(day, dailySums.getOrDefault(day, 0.0) + action.getValue());
                    }
                }
            }

            for (Map.Entry<Integer, Double> entry : dailySums.entrySet()) {
                if (entry.getValue() >= target) {
                    perfectArr[entry.getKey()] = 1;
                }
            }

            String unitVn = habit.getDonVi();
            if ("hours".equalsIgnoreCase(unitVn)) unitVn = "Giờ";
            else if ("pages".equalsIgnoreCase(unitVn)) unitVn = "Trang";

            tvDetail.setText(habit.getTen());
            txtTTD.setText(databaseHelper.getTotalDaysWithProgress(idTaiKhoan, idHabit) + " Ngày");
            txtVTT.setText(String.format(Locale.getDefault(), "%.1f %s", databaseHelper.getTotalProgress(idTaiKhoan, idHabit), unitVn));
            txtDIM.setText(databaseHelper.getDoneInCurrentMonth(idTaiKhoan, idHabit) + " Ngày");
            txtCS.setText(calculateStreak() + " Ngày");

            if (tvHabitDesc != null) tvHabitDesc.setText(habit.getMoTa());
            if (tvHabitSchedule != null) {
                String periodVn = habit.getKhoangThoiGian();
                if ("Day".equalsIgnoreCase(periodVn) || "Ngày".equalsIgnoreCase(periodVn)) periodVn = "Hàng ngày";
                else if ("Week".equalsIgnoreCase(periodVn) || "Tuần".equalsIgnoreCase(periodVn)) periodVn = "Hàng tuần";
                else if ("Month".equalsIgnoreCase(periodVn) || "Tháng".equalsIgnoreCase(periodVn)) periodVn = "Hàng tháng";
                tvHabitSchedule.setText(periodVn);
            }
            if (tvHabitTime != null) {
                String timeVn = habit.getThoiDiem();
                if ("Morning".equalsIgnoreCase(timeVn) || "Sáng".equalsIgnoreCase(timeVn)) timeVn = "Buổi sáng";
                else if ("Afternoon".equalsIgnoreCase(timeVn) || "Chiều".equalsIgnoreCase(timeVn)) timeVn = "Buổi chiều";
                else if ("Evening".equalsIgnoreCase(timeVn) || "Tối".equalsIgnoreCase(timeVn)) timeVn = "Buổi tối";
                else if ("Anytime".equalsIgnoreCase(timeVn) || "Mọi lúc".equalsIgnoreCase(timeVn)) timeVn = "Cả ngày";
                tvHabitTime.setText(timeVn);
            }
            
            // Hiển thị đúng thời hạn thực hiện
            if (tvHabitDuration != null) {
                tvHabitDuration.setText(habit.getThoiGianBatDau() + " - " + habit.getThoiGianKetThuc());
            }
            
        } catch (Exception e) {
            Log.e("ProgressActivity", "Error", e);
        }
    }

    private int calculateStreak() {
        int max = 0, current = 0;
        int today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        
        for (int i = 1; i <= today; i++) {
            if (perfectArr[i] == 1) {
                current++;
                max = Math.max(max, current);
            } else {
                current = 0;
            }
        }
        return current;
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xoá thói quen")
                .setMessage("Bạn có chắc chắn muốn xoá thói quen này không?")
                .setPositiveButton("Xoá", (d, w) -> {
                    databaseHelper.deleteHabit(idTaiKhoan, idHabit);
                    Toast.makeText(this, "Đã xoá thói quen", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void showInforDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_infor);
        
        Habit h = databaseHelper.getHabit(idTaiKhoan, idHabit);
        if (h != null) {
            ((EditText)dialog.findViewById(R.id.edt_name)).setText(h.getTen());
            ((EditText)dialog.findViewById(R.id.edt_decription)).setText(h.getMoTa());
            ((EditText)dialog.findViewById(R.id.edt_goal)).setText(String.valueOf(h.getMucTieu()));
            ((EditText)dialog.findViewById(R.id.edt_unit)).setText(h.getDonVi());
            ((EditText)dialog.findViewById(R.id.edt_start_term)).setText(h.getThoiGianBatDau());
            ((EditText)dialog.findViewById(R.id.edt_end_term)).setText(h.getThoiGianKetThuc());
            ((EditText)dialog.findViewById(R.id.edt_reminder)).setText(h.getThoiGianNhacNho());
            
            ((EditText)dialog.findViewById(R.id.edt_period)).setText(h.getKhoangThoiGian());
            ((EditText)dialog.findViewById(R.id.edt_increase)).setText(String.valueOf(h.getDonViTang()));
            ((EditText)dialog.findViewById(R.id.edt_time_range)).setText(h.getThoiDiem());
        }

        dialog.findViewById(R.id.img_close).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }
}
