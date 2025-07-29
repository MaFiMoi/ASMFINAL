package com.example.asmfinal.Session;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.asmfinal.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // This refers to the layout with "Hello World!" and the button

        Button myButton = findViewById(R.id.Idbtn); // Get reference to the button

        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start RegisterActivity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent); // Start the new activity
            }
        });
    }
}