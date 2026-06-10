package com.example.finalproject.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputType;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.R;
import com.example.finalproject.model.Account;
import com.example.finalproject.model.Habit;
import com.example.finalproject.model.HabitDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Edit_habit extends AppCompatActivity {
    private HabitDatabaseHelper databaseHelper;
    private String isTimeRangeSelected = "Anytime";
    private int defaultColor = Color.parseColor("#d0ebff");
    private int selectedColor = Color.parseColor("#187BCE");
    private final int[] clickCount = {0};
    private String period = "Day";

    private boolean isDaySelected = false;
    private boolean isWeekSelected = false;
    private boolean isMonthSelected = false;
    private int implementationDays = 1;

    private Button btnComplete;
    private ImageButton btnBack;
    private String idTaiKhoan;
    private String idThoiQuen;
    private TextView txtIncrease;
    private EditText editName, editDescription, editReminderMessage, editNumber;
    private Button btnMorning, btnAfternoon, btnEvening, btnAnytime, btntime;
    private Button btnDonVi, btnDay, btnWeek, btnMonth;
    private Button btnBatDau, btnKetThuc;
    private Account acc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_habit);
        
        databaseHelper = HabitDatabaseHelper.getInstance(this);
        
        idTaiKhoan = getIntent().getStringExtra("idTaiKhoan");
        idThoiQuen = getIntent().getStringExtra("idThoiQuen");
        acc = (Account) getIntent().getSerializableExtra("user_account");

        initWidgets();
        showInfor();

        btnComplete.setOnClickListener(v -> changeHabit());
        btnBack.setOnClickListener(v -> finish());
    }

    private void initWidgets() {
        btnComplete = findViewById(R.id.btnComplete);
        editName = findViewById(R.id.editName);
        editDescription = findViewById(R.id.editDescription);
        txtIncrease = findViewById(R.id.txtIncrease);
        editReminderMessage = findViewById(R.id.editReminderMessage);
        editNumber = findViewById(R.id.editNumber);
        btntime = findViewById(R.id.btntime);
        btnBatDau = findViewById(R.id.btnBatDau);
        btnKetThuc = findViewById(R.id.btnKetThuc);
        btnDonVi = findViewById(R.id.btnDonVi);
        btnBack = findViewById(R.id.btnBack);
        
        btnMorning = findViewById(R.id.btnMorning);
        btnAfternoon = findViewById(R.id.btnAfternoon);
        btnEvening = findViewById(R.id.btnEvening);
        btnAnytime = findViewById(R.id.btnAnytime);
        
        btnDay = findViewById(R.id.btnDay);
        btnWeek = findViewById(R.id.btnWeek);
        btnMonth = findViewById(R.id.btnMonth);

        setupTimeRangeListeners();
        setupGoalListeners();
        setupDatePickers();
        setupPeriodListeners();
    }

    private void showInfor() {
        Habit habit = databaseHelper.getHabit(idTaiKhoan, idThoiQuen);
        if (habit == null) {
            Toast.makeText(this, "Không tìm thấy dữ liệu thói quen", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editName.setText(habit.getTen());
        editDescription.setText(habit.getMoTa());
        txtIncrease.setText(String.valueOf(habit.getDonViTang()));
        editReminderMessage.setText(habit.getLoiNhacNho());
        editNumber.setText(String.valueOf(habit.getMucTieu()));
        btntime.setText(habit.getThoiGianNhacNho());
        btnKetThuc.setText(habit.getThoiGianKetThuc());
        btnBatDau.setText(habit.getThoiGianBatDau());
        btnDonVi.setText(habit.getDonVi());
        
        isTimeRangeSelected = habit.getThoiDiem();
        updateTimeRangeUI();
        
        period = habit.getKhoangThoiGian();
        updatePeriodUI();
    }

    private void changeHabit() {
        String name = editName.getText().toString().trim();
        String goalStr = editNumber.getText().toString().trim();
        
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(goalStr)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Habit habit = databaseHelper.getHabit(idTaiKhoan, idThoiQuen);
        if (habit == null) habit = new Habit();

        habit.setTen(name);
        habit.setMoTa(editDescription.getText().toString());
        habit.setMucTieu(Double.parseDouble(goalStr));
        habit.setDonVi(btnDonVi.getText().toString());
        habit.setDonViTang(Double.parseDouble(txtIncrease.getText().toString()));
        habit.setThoiDiem(isTimeRangeSelected);
        habit.setThoiGianNhacNho(btntime.getText().toString());
        habit.setThoiGianBatDau(btnBatDau.getText().toString());
        habit.setThoiGianKetThuc(btnKetThuc.getText().toString());
        habit.setLoiNhacNho(editReminderMessage.getText().toString());
        habit.setKhoangThoiGian(period);

        if (databaseHelper.updateHabit(idTaiKhoan, idThoiQuen, habit)) {
            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupTimeRangeListeners() {
        View.OnClickListener listener = v -> {
            btnMorning.setBackgroundColor(defaultColor);
            btnAfternoon.setBackgroundColor(defaultColor);
            btnEvening.setBackgroundColor(defaultColor);
            btnAnytime.setBackgroundColor(defaultColor);
            v.setBackgroundColor(selectedColor);
            
            if (v.getId() == R.id.btnMorning) isTimeRangeSelected = "Morning";
            else if (v.getId() == R.id.btnAfternoon) isTimeRangeSelected = "Afternoon";
            else if (v.getId() == R.id.btnEvening) isTimeRangeSelected = "Evening";
            else isTimeRangeSelected = "Anytime";
        };
        btnMorning.setOnClickListener(listener);
        btnAfternoon.setOnClickListener(listener);
        btnEvening.setOnClickListener(listener);
        btnAnytime.setOnClickListener(listener);
    }

    private void updateTimeRangeUI() {
        btnMorning.setBackgroundColor(defaultColor);
        btnAfternoon.setBackgroundColor(defaultColor);
        btnEvening.setBackgroundColor(defaultColor);
        btnAnytime.setBackgroundColor(defaultColor);
        
        if ("Morning".equals(isTimeRangeSelected)) btnMorning.setBackgroundColor(selectedColor);
        else if ("Afternoon".equals(isTimeRangeSelected)) btnAfternoon.setBackgroundColor(selectedColor);
        else if ("Evening".equals(isTimeRangeSelected)) btnEvening.setBackgroundColor(selectedColor);
        else btnAnytime.setBackgroundColor(selectedColor);
    }

    private void setupPeriodListeners() {
        View.OnClickListener listener = v -> {
            btnDay.setBackgroundColor(defaultColor);
            btnWeek.setBackgroundColor(defaultColor);
            btnMonth.setBackgroundColor(defaultColor);
            v.setBackgroundColor(selectedColor);
            
            if (v.getId() == R.id.btnDay) period = "Day";
            else if (v.getId() == R.id.btnWeek) period = "Week";
            else period = "Month";
        };
        btnDay.setOnClickListener(listener);
        btnWeek.setOnClickListener(listener);
        btnMonth.setOnClickListener(listener);
    }

    private void updatePeriodUI() {
        btnDay.setBackgroundColor(defaultColor);
        btnWeek.setBackgroundColor(defaultColor);
        btnMonth.setBackgroundColor(defaultColor);
        
        if ("Day".equals(period)) btnDay.setBackgroundColor(selectedColor);
        else if ("Week".equals(period)) btnWeek.setBackgroundColor(selectedColor);
        else if ("Month".equals(period)) btnMonth.setBackgroundColor(selectedColor);
    }

    private void setupDatePickers() {
        btnBatDau.setOnClickListener(v -> showDatePicker(btnBatDau));
        btnKetThuc.setOnClickListener(v -> showDatePicker(btnKetThuc));
        
        btntime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                cal.set(Calendar.MINUTE, minute);
                btntime.setText(new SimpleDateFormat("hh:mm a", Locale.US).format(cal.getTime()));
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show();
        });
    }

    private void showDatePicker(Button btn) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            btn.setText(String.format(Locale.getDefault(), "%02d-%02d-%d", dayOfMonth, month + 1, year));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupGoalListeners() {
        btnDonVi.setOnClickListener(v -> {
            clickCount[0]++;
            switch (clickCount[0] % 3) {
                case 0: btnDonVi.setText("km"); txtIncrease.setText("0.1"); editNumber.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL); break;
                case 1: btnDonVi.setText("pages"); txtIncrease.setText("1"); editNumber.setInputType(InputType.TYPE_CLASS_NUMBER); break;
                case 2: btnDonVi.setText("hours"); txtIncrease.setText("0.5"); editNumber.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL); break;
            }
        });
    }
}
