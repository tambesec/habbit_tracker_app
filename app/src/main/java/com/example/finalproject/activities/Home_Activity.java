package com.example.finalproject.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.model.Account;
import com.example.finalproject.model.Habit;
import com.example.finalproject.model.HabitAction;
import com.example.finalproject.model.HabitDatabaseHelper;
import com.example.finalproject.model.ListviewHomeTest;
import com.example.finalproject.model.MyBroadcastReceiver;
import com.example.finalproject.ui.LisviewHomeTestAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.List;

public class Home_Activity extends AppCompatActivity {
    private HabitDatabaseHelper databaseHelper;
    private Account acc = new Account();
    private String idUser;
    private String selectedDate;
    ListView listHome;
    ArrayList<ListviewHomeTest> arrayListHome;
    LisviewHomeTestAdapter adapterHome;
    FloatingActionButton btnNew; 
    ImageButton ibGraph, ibClock, ibSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        databaseHelper = HabitDatabaseHelper.getInstance(this);

        Bundle b = getIntent().getExtras();
        if (b != null && b.getSerializable("user_account") != null) {
            acc = (Account) b.getSerializable("user_account");
        }
        
        idUser = getIntent().getStringExtra("idTaiKhoan");
        if (idUser == null && acc != null && acc.getUsername() != null) {
            idUser = databaseHelper.getAccountIdByUsernameAndPassword(acc.getUsername(), acc.getPassword());
        }

        TextView currentDay = findViewById(R.id.txtCurrentDay);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        selectedDate = sdf.format(new Date());
        
        if (currentDay != null) {
            currentDay.setText("Hôm nay: " + selectedDate);
        }

        listHome = findViewById(R.id.lvHome);
        arrayListHome = new ArrayList<>();
        adapterHome = new LisviewHomeTestAdapter(arrayListHome, this, R.layout.list_item_home, idUser);
        listHome.setAdapter(adapterHome);

        btnNew = findViewById(R.id.btnNew); 
        btnNew.setOnClickListener(v -> {
            Intent intent = new Intent(Home_Activity.this, Create_habit.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("user_account", acc);
            intent.putExtra("idTaiKhoan", idUser);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        setupBottomNav();
        notification();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (idUser == null && acc != null && acc.getUsername() != null) {
            idUser = databaseHelper.getAccountIdByUsernameAndPassword(acc.getUsername(), acc.getPassword());
        }
        if (idUser != null) {
            getListHabit();
            getReminder();
        }
    }

    private void setupBottomNav() {
        ibGraph = findViewById(R.id.ib_graph);
        ibClock = findViewById(R.id.ib_clock);
        ibSettings = findViewById(R.id.ib_settings);

        View.OnClickListener navListener = v -> {
            Class<?> target = null;
            if (v.getId() == R.id.ib_graph) target = Progress_total.class;
            else if (v.getId() == R.id.ib_clock) target = Pomorodo.class;
            else if (v.getId() == R.id.ib_settings) target = Setting.class;

            if (target != null) {
                Intent i = new Intent(Home_Activity.this, target);
                Bundle bundle = new Bundle();
                bundle.putSerializable("user_account", acc);
                i.putExtra("idTaiKhoan", idUser);
                i.putExtras(bundle);
                startActivity(i);
            }
        };

        if (ibGraph != null) ibGraph.setOnClickListener(navListener);
        if (ibClock != null) ibClock.setOnClickListener(navListener);
        if (ibSettings != null) ibSettings.setOnClickListener(navListener);
    }

    public void getListHabit() {
        if (idUser == null) return;
        arrayListHome.clear();
        List<Habit> habits = databaseHelper.getHabitsByUser(idUser);
        for (Habit habit : habits) {
            String habitId = habit.getHabitId();
            double target = calculateTarget(habit.getMucTieu(), habit.getKhoangThoiGian());
            double doing = getHistoryData(databaseHelper.getHabitActions(idUser, habitId));
            
            double progress = (target > 0) ? (doing * 100.0 / target) : 0;
            String doneText = String.format(Locale.getDefault(), "%.1f/%.1f %s", doing, target, habit.getDonVi());

            ListviewHomeTest item = new ListviewHomeTest(
                habitId, habit.getTen(), habit.getThoiDiem(), 
                habit.getThoiGianNhacNho(), doneText, (int) Math.min(100, progress), 
                habit.getDonViTang(), target, habit.getTrangThai(), doing
            );
            arrayListHome.add(item);
        }
        adapterHome.notifyDataSetChanged();
    }

    public double calculateTarget(double target, String period) {
        if (period == null) return target;
        double result = target;
        if (period.equalsIgnoreCase("Week")) result = target / 7.0;
        else if (period.equalsIgnoreCase("Month")) result = target / 30.0;
        return new BigDecimal(result).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    public double getHistoryData(List<HabitAction> actions) {
        double result = 0;
        for (HabitAction action : actions) {
            if (action.getActionTime() != null && action.getActionTime().startsWith(selectedDate)) {
                result += action.getValue();
            }
        }
        return result;
    }

    public void showHabitOptions(ListviewHomeTest item) {
        if (item == null) return;
        
        if (idUser == null && acc != null && acc.getUsername() != null) {
            idUser = databaseHelper.getAccountIdByUsernameAndPassword(acc.getUsername(), acc.getPassword());
        }

        String[] options = {"Tuỳ chỉnh", "Chỉnh sửa", "Xoá"};
        new AlertDialog.Builder(this)
                .setTitle(item.getNameHabit())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(Home_Activity.this, ProgressActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("user_account", acc);
                        intent.putExtras(bundle);
                        intent.putExtra("idThoiQuen", item.getHabitId());
                        intent.putExtra("idTaiKhoan", idUser);
                        startActivity(intent);
                    } else if (which == 1) {
                        Intent intent = new Intent(Home_Activity.this, Edit_habit.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("user_account", acc);
                        intent.putExtras(bundle);
                        intent.putExtra("idThoiQuen", item.getHabitId());
                        intent.putExtra("idTaiKhoan", idUser);
                        startActivity(intent);
                    } else if (which == 2) {
                        new AlertDialog.Builder(this)
                                .setTitle("Xác nhận xoá")
                                .setMessage("Bạn có chắc muốn xoá thói quen này không?")
                                .setPositiveButton("Xoá", (d, w) -> {
                                    if (idUser != null) {
                                        databaseHelper.deleteHabit(idUser, item.getHabitId());
                                        getListHabit();
                                        Toast.makeText(this, "Đã xoá thói quen", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("Huỷ", null)
                                .show();
                    }
                })
                .show();
    }

    public void getReminder() {
        if (idUser == null) return;
        for (Habit habit : databaseHelper.getHabitsByUser(idUser)) {
            Calendar cal = databaseHelper.parseReminderTime(habit.getThoiGianNhacNho());
            if (cal != null) {
                setTimer(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), habit.getLoiNhacNho());
            }
        }
    }

    private void setTimer(int hour, int minute, String message) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar cal_alarm = Calendar.getInstance();
        cal_alarm.set(Calendar.HOUR_OF_DAY, hour);
        cal_alarm.set(Calendar.MINUTE, minute);
        cal_alarm.set(Calendar.SECOND, 0);

        if (cal_alarm.before(Calendar.getInstance())) cal_alarm.add(Calendar.DATE, 1);

        Intent i = new Intent(this, MyBroadcastReceiver.class);
        i.putExtra("message", message);
        int requestCode = (int) System.currentTimeMillis(); 
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, i, PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal_alarm.getTimeInMillis(), pendingIntent);
    }

    private void notification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Notify", "Habit Reminders", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }
}
