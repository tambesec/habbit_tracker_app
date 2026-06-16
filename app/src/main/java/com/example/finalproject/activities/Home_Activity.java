package com.example.finalproject.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
    View btnNew; 
    ImageButton ibGraph, ibClock, ibSettings;

    private static final int NOTIFICATION_PERMISSION_CODE = 123;

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
        if (btnNew != null) {
            btnNew.setOnClickListener(v -> {
                Intent intent = new Intent(Home_Activity.this, Create_habit.class);
                intent.putExtra("idTaiKhoan", idUser);
                intent.putExtra("user_account", acc);
                startActivity(intent);
            });
        }

        setupBottomNav();
        checkAndRequestNotificationPermission();
        createNotificationChannel();
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Đã bật thông báo nhắc nhở", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Vui lòng cấp quyền thông báo để nhận nhắc nhở", Toast.LENGTH_LONG).show();
            }
        }
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
                i.putExtra("idTaiKhoan", idUser);
                i.putExtra("user_account", acc);
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
            double target = habit.getMucTieu(); 
            double doing = getProgressByPeriod(habitId, habit.getKhoangThoiGian());
            double progress = (target > 0) ? (doing * 100.0 / target) : 0;
            
            String unitVn = habit.getDonVi();
            if ("hours".equalsIgnoreCase(unitVn)) unitVn = "Giờ";
            else if ("pages".equalsIgnoreCase(unitVn)) unitVn = "Trang";
            else if ("km".equalsIgnoreCase(unitVn)) unitVn = "km";
            
            String doneText = String.format(Locale.getDefault(), "%.1f/%.1f %s", doing, target, unitVn);

            String thoiDiemVn = habit.getThoiDiem();
            if ("Morning".equalsIgnoreCase(thoiDiemVn)) thoiDiemVn = "Sáng";
            else if ("Afternoon".equalsIgnoreCase(thoiDiemVn)) thoiDiemVn = "Chiều";
            else if ("Evening".equalsIgnoreCase(thoiDiemVn)) thoiDiemVn = "Tối";
            else if ("Anytime".equalsIgnoreCase(thoiDiemVn)) thoiDiemVn = "Mọi lúc";

            // SỬA ĐỔI: Tự động đánh dấu hoàn thành nếu đạt mục tiêu
            String status = habit.getTrangThai();
            if (doing >= target && target > 0) {
                status = "Đã hoàn thành";
            }

            ListviewHomeTest item = new ListviewHomeTest(
                habitId, habit.getTen(), thoiDiemVn, 
                habit.getThoiGianNhacNho(), doneText, (int) Math.min(100, progress), 
                habit.getDonViTang(), target, status, doing
            );
            arrayListHome.add(item);
        }
        adapterHome.notifyDataSetChanged();
    }

    private double getProgressByPeriod(String habitId, String period) {
        if (period == null) period = "Ngày";
        if (period.equalsIgnoreCase("Day") || period.equalsIgnoreCase("Ngày")) {
            return getHistoryData(databaseHelper.getHabitActions(idUser, habitId), 0);
        } else if (period.equalsIgnoreCase("Week") || period.equalsIgnoreCase("Tuần")) {
            return getHistoryData(databaseHelper.getHabitActions(idUser, habitId), 7);
        } else if (period.equalsIgnoreCase("Month") || period.equalsIgnoreCase("Tháng")) {
            return getHistoryData(databaseHelper.getHabitActions(idUser, habitId), 30);
        }
        return getHistoryData(databaseHelper.getHabitActions(idUser, habitId), 0);
    }

    public double getHistoryData(List<HabitAction> actions, int daysBack) {
        double result = 0;
        if (daysBack <= 0) {
            for (HabitAction action : actions) {
                if (action.getActionTime() != null && action.getActionTime().startsWith(selectedDate)) {
                    result += action.getValue();
                }
            }
        } else {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -daysBack);
            Date startDate = cal.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
            for (HabitAction action : actions) {
                try {
                    Date actionDate = sdf.parse(action.getActionTime());
                    if (actionDate != null && actionDate.after(startDate)) {
                        result += action.getValue();
                    }
                } catch (Exception ignored) {}
            }
        }
        return result;
    }

    public void showHabitOptions(ListviewHomeTest item) {
        if (item == null) return;
        String[] options = {"Xem tiến độ", "Chỉnh sửa", "Xoá thói quen"};
        new AlertDialog.Builder(this)
                .setTitle(item.getNameHabit())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(Home_Activity.this, ProgressActivity.class);
                        intent.putExtra("idThoiQuen", item.getHabitId());
                        intent.putExtra("idTaiKhoan", idUser);
                        intent.putExtra("user_account", acc);
                        startActivity(intent);
                    } else if (which == 1) {
                        Intent intent = new Intent(Home_Activity.this, Edit_habit.class);
                        intent.putExtra("idThoiQuen", item.getHabitId());
                        intent.putExtra("idTaiKhoan", idUser);
                        intent.putExtra("user_account", acc);
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
        int requestCode = message.hashCode() + (int)cal_alarm.getTimeInMillis(); 
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, i, PendingIntent.FLAG_IMMUTABLE);
        
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal_alarm.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal_alarm.getTimeInMillis(), pendingIntent);
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Notify", "Habit Reminders", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel for Habit Tracker Reminders");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }
}
