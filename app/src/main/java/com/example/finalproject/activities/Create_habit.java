package com.example.finalproject.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.model.Account;
import com.example.finalproject.model.Habit;
import com.example.finalproject.model.HabitDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Create_habit extends AppCompatActivity {

    private HabitDatabaseHelper databaseHelper;
    private String isTimeRangeSelected = "Mọi lúc";
    private int defaultColor = Color.parseColor("#F3E5F5");
    private int selectedColor = Color.parseColor("#187BCE");
    private int selectedTextColor = Color.WHITE;
    private int defaultTextColor = Color.BLACK;
    
    private final int[] clickCount = { 0 };
    private String period = "Ngày";

    private String idUser;
    private EditText editName, editDescription, editReminderMessage, editNumber, editIncrease;
    private Button btnMorning, btnAfternoon, btnEvening, btnAnytime, btntime;
    private Button btnDonVi, btnDay, btnWeek, btnMonth;
    private Button btnBatDau, btnKetThuc;
    private Button btnIncreaseInc, btnDecreaseInc, btnComplete;
    private ImageButton btnBack;
    private Account acc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_habit);

        databaseHelper = HabitDatabaseHelper.getInstance(this);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            acc = (Account) b.getSerializable("user_account");
            idUser = getIntent().getStringExtra("idTaiKhoan");
        }
        
        if (idUser == null && acc != null && acc.getUsername() != null) {
            idUser = databaseHelper.getAccountIdByUsernameAndPassword(acc.getUsername(), acc.getPassword());
        }

        initViews();
        setupListeners();
        
        // Cập nhật giao diện mặc định ban đầu
        updatePeriodUI();
        updateTimeRangeUI();
    }

    private void initViews() {
        editName = findViewById(R.id.editName);
        editDescription = findViewById(R.id.editDescription);
        editIncrease = findViewById(R.id.editIncrease);
        editReminderMessage = findViewById(R.id.editReminderMessage);
        editNumber = findViewById(R.id.editNumber);
        btnBack = findViewById(R.id.btnBack);
        btnBatDau = findViewById(R.id.btnBatDau);
        btnKetThuc = findViewById(R.id.btnKetThuc);
        btnComplete = findViewById(R.id.btnComplete);
        btntime = findViewById(R.id.btntime);
        btnDonVi = findViewById(R.id.btnDonVi);
        btnDay = findViewById(R.id.btnDay);
        btnWeek = findViewById(R.id.btnWeek);
        btnMonth = findViewById(R.id.btnMonth);
        btnIncreaseInc = findViewById(R.id.btnIncreaseInc);
        btnDecreaseInc = findViewById(R.id.btnDecreaseInc);
        btnMorning = findViewById(R.id.btnMorning);
        btnAfternoon = findViewById(R.id.btnAfternoon);
        btnEvening = findViewById(R.id.btnEvening);
        btnAnytime = findViewById(R.id.btnAnytime);
        
        // Cập nhật text nút "Any" trong XML thành "Mọi lúc" nếu cần, hoặc set ở đây
        btnAnytime.setText("Mọi lúc");
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnComplete.setOnClickListener(v -> addHabit());
        btnBatDau.setOnClickListener(v -> showDatePicker(btnBatDau));
        btnKetThuc.setOnClickListener(v -> showDatePicker(btnKetThuc));
        
        // Xử lý chọn Thời điểm (Sáng/Chiều/Tối/Mọi lúc)
        View.OnClickListener timeListener = v -> {
            if (v.getId() == R.id.btnMorning) isTimeRangeSelected = "Sáng";
            else if (v.getId() == R.id.btnAfternoon) isTimeRangeSelected = "Chiều";
            else if (v.getId() == R.id.btnEvening) isTimeRangeSelected = "Tối";
            else isTimeRangeSelected = "Mọi lúc";
            updateTimeRangeUI();
        };
        btnMorning.setOnClickListener(timeListener);
        btnAfternoon.setOnClickListener(timeListener);
        btnEvening.setOnClickListener(timeListener);
        btnAnytime.setOnClickListener(timeListener);

        // Xử lý chọn Tần suất (Ngày/Tuần/Tháng)
        View.OnClickListener periodListener = v -> {
            if (v.getId() == R.id.btnDay) period = "Ngày";
            else if (v.getId() == R.id.btnWeek) period = "Tuần";
            else period = "Tháng";
            updatePeriodUI();
        };
        btnDay.setOnClickListener(periodListener);
        btnWeek.setOnClickListener(periodListener);
        btnMonth.setOnClickListener(periodListener);

        // Nút tăng/giảm đơn vị
        btnIncreaseInc.setOnClickListener(v -> adjustIncrease(0.1));
        btnDecreaseInc.setOnClickListener(v -> adjustIncrease(-0.1));

        // Nút đổi đơn vị nhanh (km/Trang/Giờ)
        btnDonVi.setOnClickListener(v -> {
            clickCount[0]++;
            switch (clickCount[0] % 3) {
                case 0: btnDonVi.setText("km"); editIncrease.setText("0.1"); editNumber.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL); break;
                case 1: btnDonVi.setText("Trang"); editIncrease.setText("1"); editNumber.setInputType(InputType.TYPE_CLASS_NUMBER); break;
                case 2: btnDonVi.setText("Giờ"); editIncrease.setText("0.5"); editNumber.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL); break;
            }
        });

        // Chọn giờ nhắc nhở
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

    private void adjustIncrease(double delta) {
        try {
            double current = Double.parseDouble(editIncrease.getText().toString());
            double newVal = Math.max(0.1, Math.round((current + delta) * 10.0) / 10.0);
            editIncrease.setText(String.valueOf(newVal));
        } catch (Exception e) {
            editIncrease.setText("0.1");
        }
    }

    private void updateTimeRangeUI() {
        btnMorning.setBackgroundColor(isTimeRangeSelected.equals("Sáng") ? selectedColor : defaultColor);
        btnMorning.setTextColor(isTimeRangeSelected.equals("Sáng") ? selectedTextColor : defaultTextColor);
        btnAfternoon.setBackgroundColor(isTimeRangeSelected.equals("Chiều") ? selectedColor : defaultColor);
        btnAfternoon.setTextColor(isTimeRangeSelected.equals("Chiều") ? selectedTextColor : defaultTextColor);
        btnEvening.setBackgroundColor(isTimeRangeSelected.equals("Tối") ? selectedColor : defaultColor);
        btnEvening.setTextColor(isTimeRangeSelected.equals("Tối") ? selectedTextColor : defaultTextColor);
        btnAnytime.setBackgroundColor(isTimeRangeSelected.equals("Mọi lúc") ? selectedColor : defaultColor);
        btnAnytime.setTextColor(isTimeRangeSelected.equals("Mọi lúc") ? selectedTextColor : defaultTextColor);
    }

    private void updatePeriodUI() {
        btnDay.setBackgroundColor(period.equals("Ngày") ? selectedColor : defaultColor);
        btnDay.setTextColor(period.equals("Ngày") ? selectedTextColor : defaultTextColor);
        btnWeek.setBackgroundColor(period.equals("Tuần") ? selectedColor : defaultColor);
        btnWeek.setTextColor(period.equals("Tuần") ? selectedTextColor : defaultTextColor);
        btnMonth.setBackgroundColor(period.equals("Tháng") ? selectedColor : defaultColor);
        btnMonth.setTextColor(period.equals("Tháng") ? selectedTextColor : defaultTextColor);
    }

    private void addHabit() {
        String name = editName.getText().toString().trim();
        String goalStr = editNumber.getText().toString().trim();
        String start = btnBatDau.getText().toString();
        String end = btnKetThuc.getText().toString();
        String reminderTime = btntime.getText().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(goalStr) || 
            start.equals("Ngày bắt đầu") || end.equals("Ngày kết thúc") || reminderTime.equals("Hẹn giờ")) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isStartDateAfterEndDate(start, end)) {
            Toast.makeText(this, "Ngày bắt đầu không thể sau ngày kết thúc", Toast.LENGTH_SHORT).show();
            return;
        }

        Habit habit = new Habit();
        habit.setTen(name);
        habit.setDonVi(btnDonVi.getText().toString());
        habit.setDonViTang(Double.parseDouble(editIncrease.getText().toString()));
        habit.setKhoangThoiGian(period);
        habit.setLoiNhacNho(editReminderMessage.getText().toString());
        habit.setMoTa(editDescription.getText().toString());
        habit.setMucTieu(Double.parseDouble(goalStr));
        habit.setThoiDiem(isTimeRangeSelected);
        habit.setThoiGianBatDau(start);
        habit.setThoiGianKetThuc(end);
        habit.setThoiGianNhacNho(reminderTime);
        habit.setTrangThai("Đang thực hiện");

        databaseHelper.insertHabit(idUser, habit);
        Toast.makeText(this, "Thêm thói quen thành công", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showDatePicker(Button button) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            button.setText(String.format(Locale.getDefault(), "%02d-%02d-%d", day, month + 1, year));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
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
