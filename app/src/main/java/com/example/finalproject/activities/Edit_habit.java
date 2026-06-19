package com.example.finalproject.activities;

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
    private String isTimeRangeSelected = "Mọi lúc";
    private int defaultColor = Color.parseColor("#F3E5F5");
    private int selectedColor = Color.parseColor("#187BCE");
    private int selectedTextColor = Color.WHITE;
    private int defaultTextColor = Color.BLACK;
    private final int[] clickCount = {0};
    private String period = "Ngày";

    private Button btnComplete;
    private ImageButton btnBack;
    private String idTaiKhoan;
    private String idThoiQuen;
    private EditText editName, editDescription, editReminderMessage, editNumber, editIncrease;
    private Button btnMorning, btnAfternoon, btnEvening, btnAnytime, btntime;
    private Button btnDonVi, btnDay, btnWeek, btnMonth;
    private Button btnBatDau, btnKetThuc;
    private Button btnIncreaseInc, btnDecreaseInc;
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
        editIncrease = findViewById(R.id.editIncrease);
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

        btnIncreaseInc = findViewById(R.id.btnIncreaseInc);
        btnDecreaseInc = findViewById(R.id.btnDecreaseInc);

        btnAnytime.setText("Mọi lúc");

        setupTimeRangeListeners();
        setupGoalListeners();
        setupDatePickers();
        setupPeriodListeners();
        setupIncreaseButtons();
    }

    private void setupIncreaseButtons() {
        btnIncreaseInc.setOnClickListener(v -> {
            try {
                double current = Double.parseDouble(editIncrease.getText().toString());
                editIncrease.setText(String.valueOf(Math.round((current + 0.1) * 10.0) / 10.0));
            } catch (Exception e) {
                editIncrease.setText("0.1");
            }
        });

        btnDecreaseInc.setOnClickListener(v -> {
            try {
                double current = Double.parseDouble(editIncrease.getText().toString());
                if (current > 0.1) {
                    editIncrease.setText(String.valueOf(Math.round((current - 0.1) * 10.0) / 10.0));
                }
            } catch (Exception e) {
                editIncrease.setText("0.1");
            }
        });
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
        editIncrease.setText(String.valueOf(habit.getDonViTang()));
        editReminderMessage.setText(habit.getLoiNhacNho());
        editNumber.setText(String.valueOf(habit.getMucTieu()));
        btntime.setText(habit.getThoiGianNhacNho());
        btnKetThuc.setText(habit.getThoiGianKetThuc());
        btnBatDau.setText(habit.getThoiGianBatDau());
        btnDonVi.setText(habit.getDonVi());
        
        isTimeRangeSelected = habit.getThoiDiem();
        // Đồng bộ hóa giá trị cũ từ tiếng Anh sang tiếng Việt nếu cần
        if ("Morning".equalsIgnoreCase(isTimeRangeSelected)) isTimeRangeSelected = "Sáng";
        else if ("Afternoon".equalsIgnoreCase(isTimeRangeSelected)) isTimeRangeSelected = "Chiều";
        else if ("Evening".equalsIgnoreCase(isTimeRangeSelected)) isTimeRangeSelected = "Tối";
        else if ("Anytime".equalsIgnoreCase(isTimeRangeSelected)) isTimeRangeSelected = "Mọi lúc";
        updateTimeRangeUI();
        
        period = habit.getKhoangThoiGian();
        if ("Day".equalsIgnoreCase(period)) period = "Ngày";
        else if ("Week".equalsIgnoreCase(period)) period = "Tuần";
        else if ("Month".equalsIgnoreCase(period)) period = "Tháng";
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

        // Xử lý đơn vị tăng mà người dùng nhập vào
        double increase = 0.1;
        try {
            increase = Double.parseDouble(editIncrease.getText().toString());
        } catch (Exception ignored) {}

        habit.setTen(name);
        habit.setMoTa(editDescription.getText().toString());
        habit.setMucTieu(Double.parseDouble(goalStr));
        habit.setDonVi(btnDonVi.getText().toString());
        habit.setDonViTang(increase);
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
            if (v.getId() == R.id.btnMorning) isTimeRangeSelected = "Sáng";
            else if (v.getId() == R.id.btnAfternoon) isTimeRangeSelected = "Chiều";
            else if (v.getId() == R.id.btnEvening) isTimeRangeSelected = "Tối";
            else isTimeRangeSelected = "Mọi lúc";
            updateTimeRangeUI();
        };
        btnMorning.setOnClickListener(listener);
        btnAfternoon.setOnClickListener(listener);
        btnEvening.setOnClickListener(listener);
        btnAnytime.setOnClickListener(listener);
    }

    private void updateTimeRangeUI() {
        if (btnMorning == null) return;
        btnMorning.setBackgroundColor(isTimeRangeSelected.equals("Sáng") ? selectedColor : defaultColor);
        btnMorning.setTextColor(isTimeRangeSelected.equals("Sáng") ? selectedTextColor : defaultTextColor);
        btnAfternoon.setBackgroundColor(isTimeRangeSelected.equals("Chiều") ? selectedColor : defaultColor);
        btnAfternoon.setTextColor(isTimeRangeSelected.equals("Chiều") ? selectedTextColor : defaultTextColor);
        btnEvening.setBackgroundColor(isTimeRangeSelected.equals("Tối") ? selectedColor : defaultColor);
        btnEvening.setTextColor(isTimeRangeSelected.equals("Tối") ? selectedTextColor : defaultTextColor);
        btnAnytime.setBackgroundColor(isTimeRangeSelected.equals("Mọi lúc") ? selectedColor : defaultColor);
        btnAnytime.setTextColor(isTimeRangeSelected.equals("Mọi lúc") ? selectedTextColor : defaultTextColor);
    }

    private void setupPeriodListeners() {
        View.OnClickListener listener = v -> {
            if (v.getId() == R.id.btnDay) period = "Ngày";
            else if (v.getId() == R.id.btnWeek) period = "Tuần";
            else period = "Tháng";
            updatePeriodUI();
        };
        btnDay.setOnClickListener(listener);
        btnWeek.setOnClickListener(listener);
        btnMonth.setOnClickListener(listener);
    }

    private void updatePeriodUI() {
        btnDay.setBackgroundColor(period.equals("Ngày") ? selectedColor : defaultColor);
        btnDay.setTextColor(period.equals("Ngày") ? selectedTextColor : defaultTextColor);
        
        btnWeek.setBackgroundColor(period.equals("Tuần") ? selectedColor : defaultColor);
        btnWeek.setTextColor(period.equals("Tuần") ? selectedTextColor : defaultTextColor);
        
        btnMonth.setBackgroundColor(period.equals("Tháng") ? selectedColor : defaultColor);
        btnMonth.setTextColor(period.equals("Tháng") ? selectedTextColor : defaultTextColor);
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
                case 0: btnDonVi.setText("km"); editIncrease.setText("0.1"); editNumber.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL); break;
                case 1: btnDonVi.setText("Trang"); editIncrease.setText("1"); editNumber.setInputType(InputType.TYPE_CLASS_NUMBER); break;
                case 2: btnDonVi.setText("Giờ"); editIncrease.setText("0.5"); editNumber.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL); break;
            }
        });
    }
}
