package com.example.asmfinal.Session;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.asmfinal.R;
import com.example.asmfinal.database.DatabaseHelper;

public class LoginActivity extends AppCompatActivity {

    EditText idEmail, idPass;
    Button btnLogin, btnGoogle, btnFacebook;
    TextView tvSignUpNow;
    DatabaseHelper databaseHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        databaseHelper = new DatabaseHelper(this);

        idEmail = findViewById(R.id.idEmail);
        idPass = findViewById(R.id.idPass);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);
        tvSignUpNow = findViewById(R.id.tvSignUpNow);

        btnLogin.setOnClickListener(v -> {
            String email = idEmail.getText().toString().trim();
            String pass = idPass.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ tất cả các trường", Toast.LENGTH_SHORT).show();
            } else {
                boolean isAuthenticated = databaseHelper.checkUser(email, pass);
                if (isAuthenticated) {
                    Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                    // Correctly launch MainActivity
                    // The MainActivity is responsible for loading the HomeFragment by default.
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Close LoginActivity
                } else {
                    Toast.makeText(this, "Email hoặc mật khẩu không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnGoogle.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng đăng nhập bằng Google chưa được triển khai", Toast.LENGTH_SHORT).show();
        });

        btnFacebook.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng đăng nhập bằng Facebook chưa được triển khai", Toast.LENGTH_SHORT).show();
        });

        tvSignUpNow.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }
}