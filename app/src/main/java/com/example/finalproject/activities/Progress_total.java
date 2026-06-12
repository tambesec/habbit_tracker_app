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
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Progress_total extends AppCompatActivity {

    private ImageButton ibHome, ibClock, ibSetting;
    private TextView txtHabitDone, txtBestStreaks, txtPerfectDay; 
    private Account acc = new Account();
    private String idUser;
    private HabitDatabaseHelper databaseHelper;
    private int[] perfectArr = new int[32];
    private MaterialCalendarView calendar;
    private Set<CalendarDay> habitDays = new HashSet<>();
    private int numHabit = 0;

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
            // Quan trọng: Tắt cuộn nội bộ của lịch để thanh cuộn chính (NestedScrollView) hoạt động
            calendar.setNestedScrollingEnabled(false);
        }
    }

    public void getData() {
        try {
            Bundle b = getIntent().getExtras();
            if (b != null) {
                acc = (Account) b.getSerializable("user_account");
                if (acc == null) acc = new Account();
                idUser = getIntent().getStringExtra("idTaiKhoan");
            }

            if (idUser == null) {
                resetStats();
                return;
            }

            updateAllStatistics();
        } catch (Exception e) {
            Log.e("ProgressTotal", "Error loading data", e);
            resetStats();
        }
    }

    private void resetStats() {
        if (txtHabitDone != null) txtHabitDone.setText("0");
        if (txtPerfectDay != null) txtPerfectDay.setText("0");
        if (txtBestStreaks != null) txtBestStreaks.setText("0");
        if (calendar != null) calendar.removeDecorators();
    }

    private void updateAllStatistics() {
        numHabit = 0;
        int habitDoneCount = 0;
        Arrays.fill(perfectArr, 0);
        habitDays.clear();

        List<Habit> habits = databaseHelper.getHabitsByUser(idUser);
        
        // KIỂM TRA NẾU USER KHÔNG CÓ HABIT (Chống Crash)
        if (habits == null || habits.isEmpty()) {
            resetStats();
            return;
        }

        for (Habit habit : habits) {
            if ("Đã hoàn thành".equals(habit.getTrangThai())) {
                habitDoneCount++;
            } else if ("Đang thực hiện".equals(habit.getTrangThai())) {
                numHabit++;
                processHabitContribution(idUser, habit.getHabitId());
            }
        }

        txtHabitDone.setText(String.valueOf(habitDoneCount));

        // Chỉ tính toán nếu có ít nhất 1 thói quen đang thực hiện
        if (numHabit > 0) {
            calculateStats();
        } else {
            resetStats();
        }
        
        updateCalendarUI();
    }

    private void processHabitContribution(String userId, String habitId) {
        List<HabitAction> actions = databaseHelper.getHabitActions(userId, habitId);
        if (actions == null) return;
        
        Calendar now = Calendar.getInstance();
        int month = now.get(Calendar.MONTH);
        int year = now.get(Calendar.YEAR);

        for (HabitAction action : actions) {
            Calendar actionTime = databaseHelper.parseActionTime(action.getActionTime());
            if (actionTime != null) {
                // Chỉ tính các hành động trong tháng hiện tại
                if (actionTime.get(Calendar.MONTH) == month && actionTime.get(Calendar.YEAR) == year) {
                    int day = actionTime.get(Calendar.DAY_OF_MONTH);
                    if (day < 32) perfectArr[day]++;
                }
            }
        }
    }

    private void calculateStats() {
        int perfectDaysCount = 0;
        int maxStreak = 0;
        int currentStreak = 0;
        Calendar now = Calendar.getInstance();
        int maxDay = now.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 1; i <= maxDay; i++) {
            if (numHabit > 0 && perfectArr[i] >= numHabit) {
                perfectDaysCount++;
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
                habitDays.add(CalendarDay.from(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, i));
            } else {
                currentStreak = 0;
            }
        }
        txtPerfectDay.setText(String.valueOf(perfectDaysCount));
        txtBestStreaks.setText(String.valueOf(maxStreak));
    }

    private void updateCalendarUI() {
        if (calendar == null) return;
        calendar.removeDecorators();
        for (CalendarDay day : habitDays) {
            calendar.setDateSelected(day, true);
        }
    }

    public void getEvents() {
        if (ibHome != null) ibHome.setOnClickListener(v -> navigateTo(Home_Activity.class));
        if (ibClock != null) ibClock.setOnClickListener(v -> navigateTo(Pomorodo.class));
        if (ibSetting != null) ibSetting.setOnClickListener(v -> navigateTo(Setting.class));
    }

    private void navigateTo(Class<?> target) {
        Intent intent = new Intent(this, target);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user_account", acc);
        intent.putExtra("idTaiKhoan", idUser);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
