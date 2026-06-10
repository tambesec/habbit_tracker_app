package com.example.finalproject.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.model.Account;
import com.example.finalproject.model.Habit;
import com.example.finalproject.model.HabitDatabaseHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Create_habit extends AppCompatActivity {

    private HabitDatabaseHelper databaseHelper;
    private int implementationDays = 0;
    private String isTimeRangeSelected = "Anytime";
    private int defaultColor = Color.parseColor("#d0ebff");
    private int selectedColor = Color.parseColor("#187BCE");
    final int[] clickCount = { 0 };
    private String period = "Day";

    private boolean isDaySelected = true;
    private boolean isWeekSelected = false;
    private boolean isMonthSelected = false;

    private Button btnComplete;
    private String idUser;
    private EditText editName, editDescription, editReminderMessage, editNumber;
    private Button btnMorning, btnAfternoon, btnEvening, btnAnytime, btntime;
    private Button btnDonVi, btnDay, btnWeek, btnMonth;
    private Button btnBatDau, btnKetThuc;
    private TextView txtIncrease;
    private ImageButton btnBack;
    private Account acc = new Account();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_habit);

        // Khởi tạo databaseHelper sớm
        databaseHelper = HabitDatabaseHelper.getInstance(this);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            acc = (Account) b.getSerializable("user_account");
            if (acc == null) acc = new Account();
            idUser = getIntent().getStringExtra("idTaiKhoan");
        }

        editName = findViewById(R.id.editName);
        editDescription = findViewById(R.id.editDescription);
        txtIncrease = findViewById(R.id.txtIncrease);
        editReminderMessage = findViewById(R.id.editReminderMessage);
        editNumber = findViewById(R.id.editNumber);
        btnBack = findViewById(R.id.btnBack);
        
        btnBack.setOnClickListener(v -> finish());

        habitTerm();
        handleReminder();
        timeRange();
        handleGoal();
        
        // Mặc định chọn Day
        implementationDays = 1;
    }

    private void timeRange() {
        btnMorning = findViewById(R.id.btnMorning);
        btnAfternoon = findViewById(R.id.btnAfternoon);
        btnEvening = findViewById(R.id.btnEvening);
        btnAnytime = findViewById(R.id.btnAnytime);

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

    private void addHabit() {
        String name = editName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Vui lòng nhập tên thói quen", Toast.LENGTH_SHORT).show();
            return;
        }

        String goalStr = editNumber.getText().toString().trim();
        if (TextUtils.isEmpty(goalStr)) {
            Toast.makeText(this, "Vui lòng nhập mục tiêu", Toast.LENGTH_SHORT).show();
            return;
        }

        double goal;
        try {
            goal = Double.parseDouble(goalStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Mục tiêu không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        double increase = 0.1;
        try {
            increase = Double.parseDouble(txtIncrease.getText().toString());
        } catch (Exception ignored) {}

        String start = btnBatDau.getText().toString();
        String end = btnKetThuc.getText().toString();
        
        if (start.contains("Bắt đầu") || end.contains("Kết thúc")) {
            Toast.makeText(this, "Vui lòng chọn thời gian thực hiện", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isStartDateAfterEndDate(start, end)) {
            Toast.makeText(this, "Ngày bắt đầu không thể sau ngày kết thúc", Toast.LENGTH_SHORT).show();
            return;
        }

        period = determinePeriod();
        
        Habit habit = new Habit();
        habit.setTen(name);
        habit.setDonVi(btnDonVi.getText().toString());
        habit.setDonViTang(increase);
        habit.setKhoangThoiGian(period);
        habit.setLoiNhacNho(editReminderMessage.getText().toString());
        habit.setMoTa(editDescription.getText().toString());
        habit.setMucTieu(goal);
        habit.setThoiDiem(isTimeRangeSelected);
        habit.setThoiGianBatDau(start);
        habit.setThoiGianKetThuc(end);
        habit.setThoiGianNhacNho(btntime.getText().toString());
        habit.setTrangThai("Đang thực hiện");

        if (idUser == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseHelper.insertHabit(idUser, habit);
        Toast.makeText(this, "Thêm thói quen thành công", Toast.LENGTH_SHORT).show();
        
        Intent i = new Intent(Create_habit.this, Home_Activity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user_account", acc);
        i.putExtra("idTaiKhoan", idUser);
        i.putExtras(bundle);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    private void habitTerm() {
        btnComplete = findViewById(R.id.btnComplete);
        btnComplete.setOnClickListener(v -> addHabit());

        btnBatDau = findViewById(R.id.btnBatDau);
        btnBatDau.setOnClickListener(v -> showDatePicker(btnBatDau));

        btnKetThuc = findViewById(R.id.btnKetThuc);
        btnKetThuc.setOnClickListener(v -> showDatePicker(btnKetThuc));
    }

    private void showDatePicker(Button button) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%d", day, month + 1, year);
                    button.setText(selectedDate);
                    handleGoalPeriod();
                }, 
                calendar.get(Calendar.YEAR), 
                calendar.get(Calendar.MONTH), 
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void handleReminder() {
        btntime = findViewById(R.id.btntime);
        btntime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                c.set(Calendar.MINUTE, minute);
                SimpleDateFormat displayFormat = new SimpleDateFormat("hh:mm a", Locale.US);
                btntime.setText(displayFormat.format(c.getTime()));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
            timePickerDialog.show();
        });
    }

    private void handleGoalPeriod() {
        String startDate = btnBatDau.getText().toString();
        String endDate = btnKetThuc.getText().toString();
        
        long diff = -1;
        if (!startDate.contains("-") || !endDate.contains("-")) diff = -1;
        else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            try {
                Date d1 = sdf.parse(startDate);
                Date d2 = sdf.parse(endDate);
                diff = (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24);
            } catch (Exception e) { diff = -1; }
        }

        btnDay = findViewById(R.id.btnDay);
        btnWeek = findViewById(R.id.btnWeek);
        btnMonth = findViewById(R.id.btnMonth);

        btnWeek.setEnabled(diff >= 7);
        btnMonth.setEnabled(diff >= 30);

        View.OnClickListener listener = v -> {
            btnDay.setBackgroundColor(defaultColor);
            btnWeek.setBackgroundColor(defaultColor);
            btnMonth.setBackgroundColor(defaultColor);
            v.setBackgroundColor(selectedColor);
            
            isDaySelected = (v.getId() == R.id.btnDay);
            isWeekSelected = (v.getId() == R.id.btnWeek);
            isMonthSelected = (v.getId() == R.id.btnMonth);
            
            if (isDaySelected) implementationDays = 1;
            else if (isWeekSelected) implementationDays = 7;
            else implementationDays = 30;
        };

        btnDay.setOnClickListener(listener);
        btnWeek.setOnClickListener(listener);
        btnMonth.setOnClickListener(listener);
    }

    private String determinePeriod() {
        if (isWeekSelected) return "Week";
        if (isMonthSelected) return "Month";
        return "Day";
    }

    private void handleGoal() {
        btnDonVi = findViewById(R.id.btnDonVi);
        txtIncrease.setText("0.1");
        btnDonVi.setOnClickListener(v -> {
            clickCount[0]++;
            switch (clickCount[0] % 3) {
                case 0:
                    btnDonVi.setText("km");
                    txtIncrease.setText("0.1");
                    editNumber.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    break;
                case 1:
                    btnDonVi.setText("pages");
                    txtIncrease.setText("1");
                    editNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
                    break;
                case 2:
                    btnDonVi.setText("hours");
                    txtIncrease.setText("0.5");
                    editNumber.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    break;
            }
        });
    }

    private boolean isStartDateAfterEndDate(String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        try {
            Date dateStart = sdf.parse(startDate);
            Date dateEnd = sdf.parse(endDate);
            return dateStart != null && dateEnd != null && dateStart.after(dateEnd);
        } catch (Exception e) {
            return false;
        }
    }
}
