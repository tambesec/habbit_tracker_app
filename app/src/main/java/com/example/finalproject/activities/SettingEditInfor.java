package com.example.finalproject.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.model.Account;
import com.example.finalproject.model.HabitDatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.Calendar;
import java.util.Locale;

public class SettingEditInfor extends AppCompatActivity {

    private EditText edtName, edtPhone, edtBorn, edtGmail;
    private MaterialButton btnComplete, btnMale, btnFemale;
    private MaterialButtonToggleGroup toggleGender;
    private String idTaiKhoan, avatarUrl, username, password;
    private HabitDatabaseHelper databaseHelper;
    private Account currentAccount;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_infor);

        databaseHelper = HabitDatabaseHelper.getInstance(this);
        initWidgets();
        showInfor();
    }

    private void initWidgets() {
        edtName = findViewById(R.id.edt_name);
        edtGmail = findViewById(R.id.edt_gmail);
        edtPhone = findViewById(R.id.edt_phone);
        edtBorn = findViewById(R.id.edt_born);

        btnComplete = findViewById(R.id.btnComplete);
        btnMale = findViewById(R.id.btnMale);
        btnFemale = findViewById(R.id.btnFemale);
        toggleGender = findViewById(R.id.toggleGender);
        toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        edtBorn.setOnClickListener(v -> showDatePicker());
        btnComplete.setOnClickListener(v -> updateData());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String date = String.format(Locale.getDefault(), "%02d-%02d-%d", day, month + 1, year);
            edtBorn.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showInfor() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) return;

        currentAccount = (Account) bundle.getSerializable("user_account");
        idTaiKhoan = getIntent().getStringExtra("idTaiKhoan");

        if (currentAccount != null) {
            username = currentAccount.getUsername();
            password = currentAccount.getPassword();
            avatarUrl = currentAccount.getAvatar();

            edtName.setText(currentAccount.getName());
            edtBorn.setText(currentAccount.getBorn());
            edtPhone.setText(currentAccount.getPhone());
            edtGmail.setText(currentAccount.getGmail());

            if ("Nam".equals(currentAccount.getSex())) {
                toggleGender.check(R.id.btnMale);
            } else if ("Nữ".equals(currentAccount.getSex())) {
                toggleGender.check(R.id.btnFemale);
            }
        }
    }

    private void updateData() {
        String name = edtName.getText().toString().trim();
        String gmail = edtGmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String born = edtBorn.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(gmail)) {
            Toast.makeText(this, "Tên và Email không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidName(name)) {
            Toast.makeText(this, "Tên chỉ được chứa chữ cái và khoảng trắng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(gmail)) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(phone) && !isValidPhone(phone)) {
            Toast.makeText(this, "Số điện thoại phải gồm 10-11 chữ số", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(born) && !isValidDate(born)) {
            Toast.makeText(this, "Ngày sinh phải có định dạng dd-MM-yyyy", Toast.LENGTH_SHORT).show();
            return;
        }

        String gender = (toggleGender.getCheckedButtonId() == R.id.btnFemale) ? "Nữ" : "Nam";

        Account updatedAccount = new Account();
        updatedAccount.setName(name);
        updatedAccount.setGmail(gmail);
        updatedAccount.setPhone(phone);
        updatedAccount.setBorn(born);
        updatedAccount.setSex(gender);
        updatedAccount.setAvatar(avatarUrl);
        updatedAccount.setUsername(username);
        updatedAccount.setPassword(password);

        if (databaseHelper.updateAccount(idTaiKhoan, updatedAccount)) {
            Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, Setting.class);
            Bundle b = new Bundle();
            b.putSerializable("user_account", updatedAccount);
            intent.putExtra("idTaiKhoan", idTaiKhoan);
            intent.putExtras(b);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Lỗi khi cập nhật dữ liệu", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidName(String value) {
        return value.matches("^[\\p{L} ]{2,50}$");
    }

    private boolean isValidEmail(String value) {
        return value.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isValidPhone(String value) {
        return value.matches("^\\d{10,11}$");
    }

    private boolean isValidDate(String value) {
        return value.matches("^\\d{2}-\\d{2}-\\d{4}$");
    }
}