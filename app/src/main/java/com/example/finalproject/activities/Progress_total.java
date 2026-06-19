package com.example.finalproject.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import com.example.finalproject.R;
import com.example.finalproject.model.Account;
import com.example.finalproject.model.Habit;
import com.example.finalproject.model.HabitAction;
import com.example.finalproject.model.HabitDatabaseHelper;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Progress_total extends AppCompatActivity {

    private ImageButton ibHome, ibClock, ibSetting;
    private TextView txtHabitDone, txtBestStreaks, txtPerfectDay; 
    private Account acc = new Account();
    private String idUser;
    private HabitDatabaseHelper databaseHelper;
    private MaterialCalendarView calendar;
    private Set<CalendarDay> habitDays = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_total);
        
        databaseHelper = HabitDatabaseHelper.getInstance(this);
        initWidgets();
        getData();
        getEvents();
    }

    private void initWidgets() {
        txtHabitDone = findViewById(R.id.txtHabitDone);
        txtBestStreaks = findViewById(R.id.txtBestStreaks);
        txtPerfectDay = findViewById(R.id.txtPerfectDay);
        ibHome = findViewById(R.id.ib_home);
        ibClock = findViewById(R.id.ib_clock);
        ibSetting = findViewById(R.id.ib_settings);
        calendar = findViewById(R.id.calendarView);
        
        if (calendar != null) {
            calendar.setSelectionMode(MaterialCalendarView.SELECTION_MODE_NONE);
            calendar.setNestedScrollingEnabled(false);
        }
    }

    public void getData() {
        try {
            Bundle b = getIntent().getExtras();
            if (b != null) {
                acc = (Account) b.getSerializable("user_account");
                idUser = getIntent().getStringExtra("idTaiKhoan");
            }
            if (idUser != null) updateAllStatistics();
        } catch (Exception e) {
            Log.e("ProgressTotal", "Error loading data", e);
        }
    }

    private void updateAllStatistics() {
        int habitsCompletedCount = 0;
        habitDays.clear();
        
        List<Habit> habits = databaseHelper.getHabitsByUser(idUser);
        if (habits == null) return;

        Calendar now = Calendar.getInstance();
        int currentMonth = now.get(Calendar.MONTH);
        int currentYear = now.get(Calendar.YEAR);
        
        // Lưu trữ tiến độ hoàn thành theo ngày để tính "Ngày hoàn hảo"
        // Chỉ tính cho các thói quen "Hàng ngày"
        Map<Integer, Set<String>> dailyDoneDailyHabits = new HashMap<>();
        int totalDailyHabits = 0;

        for (Habit habit : habits) {
            String hid = habit.getHabitId();
            String period = habit.getKhoangThoiGian();
            double target = habit.getMucTieu();
            
            // 1. Tính "Thói quen hoàn thành" (Habit Done) dựa trên chu kỳ hiện tại
            double currentProgress = getProgressForCurrentPeriod(habit);
            if (currentProgress >= target && target > 0) {
                habitsCompletedCount++;
            }

            // 2. Xử lý dữ liệu cho Lịch (Chỉ thói quen Hàng ngày mới tính vào Perfect Day hằng ngày)
            if (isDaily(period)) {
                totalDailyHabits++;
                List<HabitAction> actions = databaseHelper.getHabitActions(idUser, hid);
                Map<Integer, Double> daySums = new HashMap<>();
                for (HabitAction action : actions) {
                    Calendar cal = databaseHelper.parseActionTime(action.getActionTime());
                    if (cal != null && cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear) {
                        int day = cal.get(Calendar.DAY_OF_MONTH);
                        daySums.put(day, daySums.getOrDefault(day, 0.0) + action.getValue());
                    }
                }
                for (Map.Entry<Integer, Double> entry : daySums.entrySet()) {
                    if (entry.getValue() >= target) {
                        int d = entry.getKey();
                        if (!dailyDoneDailyHabits.containsKey(d)) dailyDoneDailyHabits.put(d, new HashSet<>());
                        dailyDoneDailyHabits.get(d).add(hid);
                    }
                }
            }
        }

        txtHabitDone.setText(String.valueOf(habitsCompletedCount));

        // 3. Tính "Ngày hoàn hảo" và "Streak"
        int perfectDays = 0;
        int maxStreak = 0;
        int currentStreak = 0;
        int daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 1; i <= daysInMonth; i++) {
            Set<String> doneOnDay = dailyDoneDailyHabits.get(i);
            // Một ngày hoàn hảo là ngày hoàn thành TẤT CẢ thói quen Hàng ngày
            if (totalDailyHabits > 0 && doneOnDay != null && doneOnDay.size() >= totalDailyHabits) {
                perfectDays++;
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
                habitDays.add(CalendarDay.from(currentYear, currentMonth + 1, i));
            } else {
                currentStreak = 0;
            }
        }

        txtPerfectDay.setText(String.valueOf(perfectDays));
        txtBestStreaks.setText(String.valueOf(maxStreak));
        updateCalendarUI();
    }

    private double getProgressForCurrentPeriod(Habit habit) {
        String period = habit.getKhoangThoiGian();
        if (isDaily(period)) return databaseHelper.getTodayProgress(idUser, habit.getHabitId());
        
        int daysBack = 0;
        if (period.equalsIgnoreCase("Week") || period.equalsIgnoreCase("Tuần")) daysBack = 7;
        else if (period.equalsIgnoreCase("Month") || period.equalsIgnoreCase("Tháng")) daysBack = 30;
        
        return calculateRecentProgress(habit.getHabitId(), daysBack);
    }

    private double calculateRecentProgress(String hid, int days) {
        List<HabitAction> actions = databaseHelper.getHabitActions(idUser, hid);
        double sum = 0;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -days);
        Date startDate = cal.getTime();
        for (HabitAction action : actions) {
            Calendar actTime = databaseHelper.parseActionTime(action.getActionTime());
            if (actTime != null && actTime.getTime().after(startDate)) sum += action.getValue();
        }
        return sum;
    }

    private boolean isDaily(String p) {
        return p == null || p.equalsIgnoreCase("Day") || p.equalsIgnoreCase("Ngày");
    }

    private void updateCalendarUI() {
        if (calendar == null) return;
        calendar.removeDecorators();
        for (CalendarDay day : habitDays) {
            calendar.setDateSelected(day, true);
        }
    }

    public void getEvents() {
        ibHome.setOnClickListener(v -> navigateTo(Home_Activity.class));
        ibClock.setOnClickListener(v -> navigateTo(Pomorodo.class));
        ibSetting.setOnClickListener(v -> navigateTo(Setting.class));
    }

    private void navigateTo(Class<?> target) {
        Intent intent = new Intent(this, target);
        intent.putExtra("idTaiKhoan", idUser);
        intent.putExtra("user_account", acc);
        startActivity(intent);
    }
}
