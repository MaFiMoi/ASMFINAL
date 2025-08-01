package com.example.asmfinal.Session;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.asmfinal.R;
import com.example.asmfinal.database.DatabaseHelper;
import com.example.asmfinal.model.User;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private EditText iddate, etFullName, etEmail, etPassword, etConfirmPassword;
    private Button Male, Female, btnRegister;
    private TextView tvLoginLink;
    private String selectedGender = "";
    private Button selectedGenderButton = null;
    private DatabaseHelper databaseHelper;

    private void handleGenderButtonClick(Button clickedButton) {
        if (selectedGenderButton != null) {
            selectedGenderButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.holo_blue_dark));
            selectedGenderButton.setTextColor(ContextCompat.getColorStateList(this, R.color.white));
        }

        selectedGenderButton = clickedButton;

        if (clickedButton.getId() == R.id.btnNu) {
            selectedGender = "Female";
            selectedGenderButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.pink_color));
        } else if (clickedButton.getId() == R.id.btnNam) {
            selectedGender = "Male";
            selectedGenderButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.green_light));
        }
        selectedGenderButton.setTextColor(ContextCompat.getColorStateList(this, R.color.white));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        databaseHelper = new DatabaseHelper(this);

        iddate = findViewById(R.id.idDate);
        Male = findViewById(R.id.btnNam);
        Female = findViewById(R.id.btnNu);
        tvLoginLink = findViewById(R.id.tvLoginNow);
        btnRegister = findViewById(R.id.btnRegister);
        etFullName = findViewById(R.id.idName);
        etEmail = findViewById(R.id.idEmail);
        etPassword = findViewById(R.id.idPass);
        etConfirmPassword = findViewById(R.id.idRepass);

        iddate.setOnClickListener(v -> showDatePickerDialog());

        Male.setOnClickListener(v -> handleGenderButtonClick(Male));
        Female.setOnClickListener(v -> handleGenderButtonClick(Female));

        btnRegister.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String date = iddate.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (fullName.isEmpty() || email.isEmpty() || date.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || selectedGender.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Vui lòng điền đầy đủ tất cả các trường và chọn giới tính", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(RegisterActivity.this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            } else if (databaseHelper.checkEmailExists(email)) {
                Toast.makeText(RegisterActivity.this, "Email này đã được đăng ký", Toast.LENGTH_SHORT).show();
            } else {
                // Sửa dòng code này để tạo đối tượng User với đầy đủ các tham số
                User newUser = new User(email, password, fullName, date, selectedGender);

                long newRowId = databaseHelper.addUser(newUser); // Đã sửa kiểu dữ liệu từ boolean sang long

                // Cập nhật câu lệnh if để kiểm tra thành công
                // Phương thức insert() của SQLite trả về -1 nếu có lỗi
                if (newRowId != -1) { // Kiểm tra xem ID trả về có phải là -1 không
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    clearFields();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format("%02d/%02d/%d", dayOfMonth, (monthOfYear + 1), year1);
                    iddate.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void clearFields() {
        etFullName.setText("");
        etEmail.setText("");
        iddate.setText("");
        etPassword.setText("");
        etConfirmPassword.setText("");
        selectedGender = "";
        if (selectedGenderButton != null) {
            selectedGenderButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.holo_blue_dark));
            selectedGenderButton.setTextColor(ContextCompat.getColorStateList(this, R.color.white));
            selectedGenderButton = null;
        }
    }
}