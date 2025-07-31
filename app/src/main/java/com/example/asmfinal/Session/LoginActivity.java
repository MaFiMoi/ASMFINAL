package com.example.asmfinal.Session;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.asmfinal.R;

public class LoginActivity extends AppCompatActivity {

    EditText idEmail, idPass;
    Button btnLogin, btnGoogle, btnFacebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        idEmail = findViewById(R.id.idEmail);
        idPass = findViewById(R.id.idPass);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);

        btnLogin.setOnClickListener(v -> {
            String email = idEmail.getText().toString().trim();
            String pass = idPass.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                if (email.equals("admin@gmail.com") && pass.equals("123456")) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                    // TODO: Chuyển sang màn hình chính
                } else {
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnGoogle.setOnClickListener(v -> {
            Toast.makeText(this, "Google login clicked", Toast.LENGTH_SHORT).show();
        });

        btnFacebook.setOnClickListener(v -> {
            Toast.makeText(this, "Facebook login clicked", Toast.LENGTH_SHORT).show();
        });
    }
}