// MainActivity.java
package com.example.asmfinal.Session;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.asmfinal.R;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private EditText iddate;
    private Button Male, Female;
    private TextView tvLoginLink; // Declare TextView
    private String selectedGender = "";
    private Button selectedGenderButton = null; // Biến này sẽ giữ nút giới tính hiện đang được chọn

    private void handleGenderButtonClick(Button clickedButton) {
        // 1. Đặt lại trạng thái của nút đã chọn trước đó (nếu có)
        if (selectedGenderButton != null && selectedGenderButton != clickedButton) {
            // Đặt lại màu nền về màu mặc định (ví dụ: một drawable selector màu xám)
            selectedGenderButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.holo_blue_dark));
            // Đặt lại màu chữ về màu mặc định (ví dụ: một color selector màu đen)
            selectedGenderButton.setTextColor(ContextCompat.getColorStateList(this, R.color.white));
        }

        // 2. Cập nhật nút được chọn hiện tại
        selectedGenderButton = clickedButton;

        // 3. Đặt màu sắc cho nút hiện tại được chọn (tùy thuộc vào ID của nút)
        if (clickedButton.getId() == R.id.btnNu) { // Nếu là nút Nữ
            selectedGenderButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.pink_color));
            selectedGenderButton.setTextColor(ContextCompat.getColorStateList(this, R.color.white)); // Sử dụng selector cho nữ
        } else if (clickedButton.getId() == R.id.btnNam) { // Nếu là nút Nam
            selectedGenderButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.green_light));
            selectedGenderButton.setTextColor(ContextCompat.getColorStateList(this, R.color.white)); // Sử dụng selector cho nam
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        iddate = findViewById(R.id.idDate);
        Male = findViewById(R.id.btnNam);
        Female = findViewById(R.id.btnNu);
        tvLoginLink = findViewById(R.id.tvLoginNow); // Initialize the TextView
        Button btnRegister = findViewById(R.id.btnRegister);
        EditText etFullName = findViewById(R.id.idName);
        EditText etEmail = findViewById(R.id.idEmail);
        EditText etPassword = findViewById(R.id.idPass);
        EditText etConfirmPassword = findViewById(R.id.idRepass);

        // Date Picker setup
        iddate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // Gender selection logic
        Male.setOnClickListener(v -> {
            handleGenderButtonClick(Male);
        });

        Female.setOnClickListener(v -> {
            handleGenderButtonClick(Female);
        });

        // Register button click listener
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = etFullName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String date = iddate.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                // Simple validation (you'll need more robust validation)
                if (fullName.isEmpty() || email.isEmpty() || date.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || selectedGender.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please fill all fields and select gender", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    // Here you would typically send data to a server or save locally
                }
            }
        });
        tvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start LoginActivity
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent); // Start the new activity
                finish(); // Optional: Finish current activity so user can't go back to register
            }
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

    private void selectGender(Button selectedButton) {
        // Reset colors for both buttons
        Male.setSelected(false);
        Female.setSelected(false);

        // Set selected state for the clicked button
        selectedButton.setSelected(true);
        selectedButton.setSelected(true);
        selectedButton.setBackgroundTintList(getColorStateList(R.color.green_light)); // Re-apply selector to update
        selectedButton.setTextColor(getColorStateList(R.color.white)); // Re-apply selector to update

        selectedGender = selectedButton.getText().toString();
    }
}