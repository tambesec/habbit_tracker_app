package com.example.finalproject.model;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HabitDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "HabitDatabaseHelper";
    private static final String DB_NAME = "habit_tracker_final_v1000.db";
    private static final int DB_VERSION = 1;
    private static final String PREFS_NAME = "habit_tracker_prefs";
    private static final String PREF_SEEDED = "seeded_v1000";

    private static HabitDatabaseHelper instance;
    private final Context appContext;

    public static synchronized HabitDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new HabitDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private HabitDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.appContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: Initializing database tables...");
        db.execSQL("CREATE TABLE IF NOT EXISTS accounts (id TEXT PRIMARY KEY, avatar TEXT, sex TEXT, gmail TEXT, name TEXT, password TEXT, born TEXT, phone TEXT, username TEXT UNIQUE NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS habits (id TEXT PRIMARY KEY, user_id TEXT NOT NULL, don_vi TEXT, don_vi_tang REAL, khoang_thoi_gian TEXT, loi_nhac_nho TEXT, mo_ta TEXT, muc_tieu REAL, ten TEXT, thoi_diem TEXT, thoi_gian_bat_dau TEXT, thoi_gian_ket_thuc TEXT, thoi_gian_nhac_nho TEXT, trang_thai TEXT, FOREIGN KEY(user_id) REFERENCES accounts(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE IF NOT EXISTS habit_actions (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id TEXT NOT NULL, habit_id TEXT NOT NULL, action_time TEXT NOT NULL, value REAL NOT NULL, FOREIGN KEY(user_id) REFERENCES accounts(id) ON DELETE CASCADE, FOREIGN KEY(habit_id) REFERENCES habits(id) ON DELETE CASCADE)");
        seedFromAsset(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS habit_actions");
        db.execSQL("DROP TABLE IF EXISTS habits");
        db.execSQL("DROP TABLE IF EXISTS accounts");
        onCreate(db);
    }

    private void seedFromAsset(SQLiteDatabase db) {
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(PREF_SEEDED, false)) return;

        try {
            InputStream is = appContext.getAssets().open("tracker_seed.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer); is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(json).getJSONObject("Habit_Tracker");
            
            db.beginTransaction();
            try {
                // Seed Accounts
                JSONObject accountsObj = root.getJSONObject("Tai_Khoan");
                JSONArray accKeys = accountsObj.names();
                if (accKeys != null) {
                    for (int i = 0; i < accKeys.length(); i++) {
                        String id = accKeys.getString(i);
                        JSONObject item = accountsObj.getJSONObject(id);
                        db.insertWithOnConflict("accounts", null, toAccountValuesFromJson(id, item), SQLiteDatabase.CONFLICT_REPLACE);
                    }
                }

                // Seed Data (Habits)
                JSONObject dataObj = root.optJSONObject("Du_Lieu");
                if (dataObj != null) {
                    JSONArray userIds = dataObj.names();
                    if (userIds != null) {
                        for (int i = 0; i < userIds.length(); i++) {
                            String userId = userIds.getString(i);
                            JSONObject userHabits = dataObj.getJSONObject(userId);
                            JSONArray habitIds = userHabits.names();
                            if (habitIds == null) continue;
                            for (int j = 0; j < habitIds.length(); j++) {
                                String habitId = habitIds.getString(j);
                                JSONObject h = userHabits.getJSONObject(habitId);
                                db.insertWithOnConflict("habits", null, toHabitValuesFromJson(userId, habitId, h), SQLiteDatabase.CONFLICT_REPLACE);
                            }
                        }
                    }
                }
                db.setTransactionSuccessful();
                prefs.edit().putBoolean(PREF_SEEDED, true).apply();
            } finally { db.endTransaction(); }
        } catch (Exception e) { Log.e(TAG, "Seed failed", e); }
    }

    // --- Account Methods ---
    public List<Account> getAllAccounts() {
        List<Account> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery("SELECT * FROM accounts", null)) {
            while (c.moveToNext()) list.add(readAccount(c));
        } catch (Exception ignored) {}
        return list;
    }

    public Account getAccountById(String id) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT * FROM accounts WHERE id=?", new String[]{id})) {
            if (c.moveToFirst()) return readAccount(c);
        } catch (Exception ignored) {}
        return null;
    }

    public Account getAccountByUsernameAndPassword(String u, String p) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT * FROM accounts WHERE username=? AND password=?", new String[]{u, p})) {
            if (c.moveToFirst()) return readAccount(c);
        } catch (Exception ignored) {}
        return null;
    }

    public String getAccountIdByUsernameAndPassword(String u, String p) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT id FROM accounts WHERE username=? AND password=?", new String[]{u, p})) {
            if (c.moveToFirst()) return c.getString(0);
        } catch (Exception ignored) {}
        return null;
    }

    public String getAccountIdByUsernameAndGmail(String u, String g) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT id FROM accounts WHERE username=? AND gmail=?", new String[]{u, g})) {
            if (c.moveToFirst()) return c.getString(0);
        } catch (Exception ignored) {}
        return null;
    }

    public void insertAccount(Account a) {
        ContentValues v = new ContentValues();
        v.put("id", "User" + System.currentTimeMillis());
        v.put("username", a.getUsername()); v.put("password", a.getPassword());
        v.put("name", a.getName()); v.put("gmail", a.getGmail());
        v.put("avatar", a.getAvatar()); v.put("sex", a.getSex());
        v.put("born", a.getBorn()); v.put("phone", a.getPhone());
        getWritableDatabase().insert("accounts", null, v);
    }

    public boolean updateAccount(String id, Account a) {
        ContentValues v = new ContentValues();
        v.put("avatar", a.getAvatar()); v.put("sex", a.getSex()); v.put("gmail", a.getGmail());
        v.put("name", a.getName()); v.put("born", a.getBorn()); v.put("phone", a.getPhone());
        return getWritableDatabase().update("accounts", v, "id=?", new String[]{id}) > 0;
    }

    // --- Habit Methods ---
    public List<Habit> getHabitsByUser(String uid) {
        List<Habit> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery("SELECT * FROM habits WHERE user_id=? AND trang_thai != 'Đã xóa' ORDER BY id", new String[]{uid})) {
            while (c.moveToNext()) list.add(readHabit(c));
        } catch (Exception ignored) {}
        return list;
    }

    public Habit getHabit(String uid, String hid) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT * FROM habits WHERE user_id=? AND id=?", new String[]{uid, hid})) {
            if (c.moveToFirst()) return readHabit(c);
        } catch (Exception ignored) {}
        return null;
    }

    public String insertHabit(String uid, Habit h) {
        String id = "TQ" + System.currentTimeMillis();
        getWritableDatabase().insert("habits", null, toHabitValuesFromModel(uid, h, id));
        return id;
    }

    public boolean updateHabit(String uid, String hid, Habit h) {
        return getWritableDatabase().update("habits", toHabitValuesFromModel(uid, h, hid), "id=? AND user_id=?", new String[]{hid, uid}) > 0;
    }

    public boolean updateHabitStatus(String uid, String hid, String status) {
        ContentValues v = new ContentValues();
        v.put("trang_thai", status);
        return getWritableDatabase().update("habits", v, "id=? AND user_id=?", new String[]{hid, uid}) > 0;
    }

    public boolean deleteHabit(String uid, String hid) {
        return updateHabitStatus(uid, hid, "Đã xóa");
    }

    // --- Action & Progress Methods ---
    public boolean addHabitAction(String uid, String hid, double val) {
        ContentValues v = new ContentValues();
        v.put("user_id", uid); v.put("habit_id", hid); v.put("value", val);
        v.put("action_time", new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));
        return getWritableDatabase().insert("habit_actions", null, v) != -1;
    }

    public List<HabitAction> getHabitActions(String uid, String hid) {
        List<HabitAction> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery("SELECT action_time, value FROM habit_actions WHERE user_id=? AND habit_id=? ORDER BY action_time", new String[]{uid, hid})) {
            while (c.moveToNext()) list.add(new HabitAction(c.getString(0), c.getDouble(1)));
        } catch (Exception ignored) {}
        return list;
    }

    public double getTodayProgress(String uid, String hid) {
        String today = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        try (Cursor c = getReadableDatabase().rawQuery("SELECT SUM(value) FROM habit_actions WHERE user_id=? AND habit_id=? AND action_time LIKE ?", new String[]{uid, hid, today + "%"})) {
            if (c.moveToFirst()) return c.getDouble(0);
        } catch (Exception ignored) {}
        return 0;
    }

    public double getTotalProgress(String uid, String hid) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT SUM(value) FROM habit_actions WHERE user_id=? AND habit_id=?", new String[]{uid, hid})) {
            if (c.moveToFirst()) return c.getDouble(0);
        } catch (Exception ignored) {}
        return 0;
    }

    public int getTotalDaysWithProgress(String uid, String hid) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(DISTINCT SUBSTR(action_time, 1, 10)) FROM habit_actions WHERE user_id=? AND habit_id=?", new String[]{uid, hid})) {
            if (c.moveToFirst()) return c.getInt(0);
        } catch (Exception ignored) {}
        return 0;
    }

    public int getDoneInCurrentMonth(String uid, String hid) {
        String m = new SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(new Date());
        try (Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(DISTINCT SUBSTR(action_time, 1, 10)) FROM habit_actions WHERE user_id=? AND habit_id=? AND action_time LIKE ?", new String[]{uid, hid, "%" + m + "%"})) {
            if (c.moveToFirst()) return c.getInt(0);
        } catch (Exception ignored) {}
        return 0;
    }

    public double getCurrentMonthActionCount(String uid, String hid, int day) {
        String m = new SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(new Date());
        String d = String.format(Locale.getDefault(), "%02d-%s", day, m);
        try (Cursor c = getReadableDatabase().rawQuery("SELECT SUM(value) FROM habit_actions WHERE user_id=? AND habit_id=? AND action_time LIKE ?", new String[]{uid, hid, d + "%"})) {
            if (c.moveToFirst()) return c.getDouble(0);
        } catch (Exception ignored) {}
        return 0;
    }

    // --- Utilities ---
    public boolean isToday(String t) {
        if (t == null) return false;
        return t.startsWith(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()));
    }

    public boolean isInCurrentMonth(String t) {
        if (t == null) return false;
        return t.contains(new SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(new Date()));
    }

    public Calendar parseActionTime(String v) {
        if (v == null || v.trim().isEmpty()) return null;
        String n = normalizeAmPm(v.trim());
        String[] pts = {"dd-MM-yyyy HH:mm:ss", "dd-MM-yyyy h:mm:ssa", "dd-MM-yyyy hh:mm:ssa"};
        for (String p : pts) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(p, Locale.getDefault());
                Date d = sdf.parse(n);
                if (d != null) { Calendar c = Calendar.getInstance(); c.setTime(d); return c; }
            } catch (Exception ignored) {}
        }
        return null;
    }

    public Calendar parseReminderTime(String v) {
        Calendar c = Calendar.getInstance();
        if (v == null || v.isEmpty()) return c;
        try {
            String n = normalizeAmPm(v.trim());
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            Date d = sdf.parse(n);
            if (d != null) {
                Calendar t = Calendar.getInstance(); t.setTime(d);
                c.set(Calendar.HOUR_OF_DAY, t.get(Calendar.HOUR_OF_DAY));
                c.set(Calendar.MINUTE, t.get(Calendar.MINUTE));
            }
        } catch (Exception ignored) {}
        return c;
    }

    public String normalizeAmPm(String v) {
        return v.replace("SA", "AM").replace("CH", "PM").replace("sa", "AM").replace("ch", "PM");
    }

    // --- Mappers ---
    private Account readAccount(Cursor c) {
        Account a = new Account();
        a.setUsername(c.getString(c.getColumnIndexOrThrow("username")));
        a.setPassword(c.getString(c.getColumnIndexOrThrow("password")));
        a.setName(c.getString(c.getColumnIndexOrThrow("name")));
        a.setGmail(c.getString(c.getColumnIndexOrThrow("gmail")));
        a.setAvatar(c.getString(c.getColumnIndexOrThrow("avatar")));
        a.setSex(c.getString(c.getColumnIndexOrThrow("sex")));
        a.setBorn(c.getString(c.getColumnIndexOrThrow("born")));
        a.setPhone(c.getString(c.getColumnIndexOrThrow("phone")));
        return a;
    }

    private Habit readHabit(Cursor c) {
        Habit h = new Habit();
        h.setHabitId(c.getString(c.getColumnIndexOrThrow("id")));
        h.setUserId(c.getString(c.getColumnIndexOrThrow("user_id")));
        h.setTen(c.getString(c.getColumnIndexOrThrow("ten")));
        h.setMucTieu(c.getDouble(c.getColumnIndexOrThrow("muc_tieu")));
        h.setDonVi(c.getString(c.getColumnIndexOrThrow("don_vi")));
        h.setDonViTang(c.getDouble(c.getColumnIndexOrThrow("don_vi_tang")));
        h.setKhoangThoiGian(c.getString(c.getColumnIndexOrThrow("khoang_thoi_gian")));
        h.setLoiNhacNho(c.getString(c.getColumnIndexOrThrow("loi_nhac_nho")));
        h.setMoTa(c.getString(c.getColumnIndexOrThrow("mo_ta")));
        h.setThoiDiem(c.getString(c.getColumnIndexOrThrow("thoi_diem")));
        h.setThoiGianBatDau(c.getString(c.getColumnIndexOrThrow("thoi_gian_bat_dau")));
        h.setThoiGianKetThuc(c.getString(c.getColumnIndexOrThrow("thoi_gian_ket_thuc")));
        h.setThoiGianNhacNho(c.getString(c.getColumnIndexOrThrow("thoi_gian_nhac_nho")));
        h.setTrangThai(c.getString(c.getColumnIndexOrThrow("trang_thai")));
        return h;
    }

    private ContentValues toAccountValuesFromJson(String id, JSONObject item) {
        ContentValues v = new ContentValues();
        v.put("id", id);
        v.put("username", item.optString("username", ""));
        v.put("password", item.optString("password", ""));
        v.put("name", item.optString("name", ""));
        v.put("gmail", item.optString("gmail", ""));
        v.put("avatar", item.optString("avatar", ""));
        v.put("sex", item.optString("sex", ""));
        v.put("born", item.optString("born", ""));
        v.put("phone", item.optString("phone", ""));
        return v;
    }

    private ContentValues toHabitValuesFromJson(String uid, String hid, JSONObject h) {
        ContentValues v = new ContentValues();
        v.put("id", hid); v.put("user_id", uid);
        v.put("ten", h.optString("Ten", "")); v.put("muc_tieu", h.optDouble("MucTieu", 0));
        v.put("don_vi", h.optString("DonVi", "")); v.put("don_vi_tang", h.optDouble("DonViTang", 0.1));
        v.put("khoang_thoi_gian", h.optString("KhoangThoiGian", "Day"));
        v.put("loi_nhac_nho", h.optString("LoiNhacNho", "")); v.put("mo_ta", h.optString("MoTa", ""));
        v.put("thoi_diem", h.optString("ThoiDiem", "Anytime"));
        v.put("thoi_gian_bat_dau", h.optString("ThoiGianBatDau", ""));
        v.put("thoi_gian_ket_thuc", h.optString("ThoiGianKetThuc", ""));
        v.put("thoi_gian_nhac_nho", h.optString("ThoiGianNhacNho", ""));
        v.put("trang_thai", h.optString("TrangThai", "Đang thực hiện"));
        return v;
    }

    private ContentValues toHabitValuesFromModel(String uid, Habit h, String id) {
        ContentValues v = new ContentValues();
        v.put("id", id); v.put("user_id", uid);
        v.put("ten", h.getTen()); v.put("muc_tieu", h.getMucTieu());
        v.put("don_vi", h.getDonVi()); v.put("don_vi_tang", h.getDonViTang());
        v.put("khoang_thoi_gian", h.getKhoangThoiGian()); v.put("loi_nhac_nho", h.getLoiNhacNho());
        v.put("mo_ta", h.getMoTa()); v.put("thoi_diem", h.getThoiDiem());
        v.put("thoi_gian_bat_dau", h.getThoiGianBatDau()); v.put("thoi_gian_ket_thuc", h.getThoiGianKetThuc());
        v.put("thoi_gian_nhac_nho", h.getThoiGianNhacNho()); v.put("trang_thai", h.getTrangThai());
        return v;
    }

    private ContentValues toAccountValuesFromModel(String id, Account a) {
        ContentValues v = new ContentValues();
        v.put("id", id);
        v.put("username", a.getUsername()); v.put("password", a.getPassword());
        v.put("name", a.getName()); v.put("gmail", a.getGmail());
        v.put("avatar", a.getAvatar()); v.put("sex", a.getSex());
        v.put("born", a.getBorn()); v.put("phone", a.getPhone());
        return v;
    }
}
