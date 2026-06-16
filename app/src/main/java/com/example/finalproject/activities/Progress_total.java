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

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    private int numActiveHabits = 0;

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
        numActiveHabits = 0;
        int habitDoneTodayCount = 0;
        habitDays.clear();

        List<Habit> habits = databaseHelper.getHabitsByUser(idUser);
        if (habits == null || habits.isEmpty()) {
            resetStats();
            return;
        }

        Calendar now = Calendar.getInstance();
        int currentDay = now.get(Calendar.DAY_OF_MONTH);
        int currentMonth = now.get(Calendar.MONTH);
        int currentYear = now.get(Calendar.YEAR);

        Map<Integer, Set<String>> dailyCompletions = new HashMap<>();

        for (Habit habit : habits) {
            numActiveHabits++;
            String hid = habit.getHabitId();
            double target = habit.getMucTieu();
            
            List<HabitAction> actions = databaseHelper.getHabitActions(idUser, hid);
            Map<Integer, Double> habitDailySums = new HashMap<>();
            
            for (HabitAction action : actions) {
                Calendar actionTime = databaseHelper.parseActionTime(action.getActionTime());
                if (actionTime != null && 
                    actionTime.get(Calendar.MONTH) == currentMonth && 
                    actionTime.get(Calendar.YEAR) == currentYear) {
                    
                    int day = actionTime.get(Calendar.DAY_OF_MONTH);
                    habitDailySums.put(day, habitDailySums.getOrDefault(day, 0.0) + action.getValue());
                }
            }

            for (Map.Entry<Integer, Double> entry : habitDailySums.entrySet()) {
                if (entry.getValue() >= target) {
                    int day = entry.getKey();
                    if (!dailyCompletions.containsKey(day)) {
                        dailyCompletions.put(day, new HashSet<>());
                    }
                    dailyCompletions.get(day).add(hid);
                    
                    if (day == currentDay) {
                        habitDoneTodayCount++;
                    }
                }
            }
        }

        txtHabitDone.setText(String.valueOf(habitDoneTodayCount));

        int perfectDaysCount = 0;
        int maxStreak = 0;
        int currentStreak = 0;
        int maxDayInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 1; i <= maxDayInMonth; i++) {
            Set<String> completedOnDay = dailyCompletions.get(i);
            
            if (numActiveHabits > 0 && completedOnDay != null && completedOnDay.size() >= numActiveHabits) {
                perfectDaysCount++;
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
                habitDays.add(CalendarDay.from(currentYear, currentMonth + 1, i));
            } else {
                currentStreak = 0;
            }
        }

        txtPerfectDay.setText(String.valueOf(perfectDaysCount));
        txtBestStreaks.setText(String.valueOf(maxStreak));
        
        updateCalendarUI();
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
