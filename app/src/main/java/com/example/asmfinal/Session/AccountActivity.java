package com.example.asmfinal.Session;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.asmfinal.R;
import com.example.asmfinal.database.DatabaseHelper;
import com.example.asmfinal.model.User;
import com.example.asmfinal.Session.LoginActivity; // Cần đảm bảo đường dẫn này đúng

public class AccountActivity extends AppCompatActivity {

    // Khai báo các thành phần UI
    private RelativeLayout header; // Thêm
    private LinearLayout profileSection; // Thêm
    private LinearLayout noDataContainer; // Thêm
    private LinearLayout menuAccount;
    private LinearLayout menuChangePassword;
    private LinearLayout menuLanguage;
    private LinearLayout menuExportCSV;
    private LinearLayout menuHistory; // Thêm
    private LinearLayout menuExchangeRate; // Thêm
    private Switch switchDarkMode;
    private Button btnLogin; // Khai báo là Button để khớp với XML
    private LinearLayout btnLogout; // Khai báo là LinearLayout để khớp với XML
    private TextView tvUserName;
    private TextView tvLoginStatus;

    // Instance của DatabaseHelper
    private DatabaseHelper databaseHelper;

    // Tên file SharedPreferences và key
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String KEY_DARK_MODE = "isDarkModeEnabled";
    private static final String KEY_LOGGED_IN_EMAIL = "loggedInUserEmail";

    // Request code cho màn hình login
    private static final int LOGIN_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Áp dụng Dark Mode trước khi gọi super.onCreate()
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkModeEnabled = prefs.getBoolean(KEY_DARK_MODE, false);
        if (isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.accout_activity);

        // Khởi tạo DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Khởi tạo views và thiết lập các sự kiện
        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật UI mỗi khi Activity được resumed (quay lại màn hình này)
        updateUIBasedOnLoginStatus();
    }

    /**
     * Khởi tạo các thành phần UI bằng cách tìm ID từ layout.
     */
    private void initViews() {
        header = findViewById(R.id.header);
        profileSection = findViewById(R.id.profileSection);
        noDataContainer = findViewById(R.id.noDataContainer);

        menuAccount = findViewById(R.id.menuAccount);
        menuChangePassword = findViewById(R.id.menuChangePassword);
        menuLanguage = findViewById(R.id.menuLanguage);
        menuExportCSV = findViewById(R.id.menuExportCSV);
        menuHistory = findViewById(R.id.menuHistory);
        menuExchangeRate = findViewById(R.id.menuExchangeRate);

        switchDarkMode = findViewById(R.id.switchDarkMode);

        // Khai báo đúng kiểu dữ liệu
        btnLogin = findViewById(R.id.btnLogin);
        btnLogout = findViewById(R.id.btnLogout);

        tvUserName = findViewById(R.id.tvUserName);
        tvLoginStatus = findViewById(R.id.tvLoginStatus);
    }

    /**
     * Thiết lập các sự kiện click và thay đổi trạng thái cho các thành phần UI.
     */
    private void setupListeners() {
        menuAccount.setOnClickListener(v -> {
            Toast.makeText(this, "Bạn đã click Tài khoản", Toast.LENGTH_SHORT).show();
            // TODO: Mở màn hình chi tiết tài khoản
        });

        menuChangePassword.setOnClickListener(v -> {
            Toast.makeText(this, "Bạn đã click Đổi Mật khẩu", Toast.LENGTH_SHORT).show();
            // TODO: Mở màn hình đổi mật khẩu
        });

        menuLanguage.setOnClickListener(v -> {
            Toast.makeText(this, "Bạn đã click Ngôn ngữ", Toast.LENGTH_SHORT).show();
            // TODO: Hiển thị dialog chọn ngôn ngữ
        });

        menuExportCSV.setOnClickListener(v -> {
            Toast.makeText(this, "Bạn đã click Xuất CSV", Toast.LENGTH_SHORT).show();
            // TODO: Xử lý logic xuất file CSV
        });

        menuHistory.setOnClickListener(v -> {
            Toast.makeText(this, "Bạn đã click Lịch sử", Toast.LENGTH_SHORT).show();
            // TODO: Mở màn hình lịch sử
        });

        menuExchangeRate.setOnClickListener(v -> {
            Toast.makeText(this, "Bạn đã click Tỷ giá tiền tệ", Toast.LENGTH_SHORT).show();
            // TODO: Mở màn hình tỷ giá tiền tệ
        });

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.putBoolean(KEY_DARK_MODE, isChecked);
            editor.apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Toast.makeText(this, "Chế độ Tối đã bật", Toast.LENGTH_SHORT).show();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Toast.makeText(this, "Chế độ Tối đã tắt", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogin.setOnClickListener(v -> {
            // Mở màn hình đăng nhập và chờ kết quả
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, LOGIN_REQUEST_CODE);
        });

        btnLogout.setOnClickListener(v -> {
            logoutUser();
        });
    }

    /**
     * Cập nhật giao diện người dùng dựa trên trạng thái đăng nhập.
     */
    private void updateUIBasedOnLoginStatus() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String loggedInEmail = prefs.getString(KEY_LOGGED_IN_EMAIL, null);

        // Cập nhật trạng thái của Dark Mode Switch
        boolean isDarkModeEnabled = prefs.getBoolean(KEY_DARK_MODE, false);
        switchDarkMode.setChecked(isDarkModeEnabled);

        if (loggedInEmail != null) {
            // Đã đăng nhập
            User currentUser = databaseHelper.getUserByEmail(loggedInEmail);
            if (currentUser != null) {
                tvUserName.setText(currentUser.getFullName());
                tvUserName.setVisibility(View.VISIBLE);

                profileSection.setVisibility(View.VISIBLE);
                noDataContainer.setVisibility(View.GONE);
                btnLogout.setVisibility(View.VISIBLE); // btnLogout nằm trong ScrollView
            } else {
                // Trường hợp người dùng không còn tồn tại trong DB, tự động đăng xuất
                logoutUser();
            }
        } else {
            // Chưa đăng nhập
            tvUserName.setVisibility(View.GONE);

            profileSection.setVisibility(View.GONE);
            noDataContainer.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.GONE); // btnLogout nằm trong ScrollView
        }
    }

    /**
     * Xử lý kết quả trả về từ một Activity khác (ví dụ: LoginActivity).
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("loggedInEmail")) {
                String email = data.getStringExtra("loggedInEmail");
                // Lưu email của người dùng đã đăng nhập thành công vào SharedPreferences
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString(KEY_LOGGED_IN_EMAIL, email);
                editor.apply();

                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                updateUIBasedOnLoginStatus();
            }
        }
    }

    /**
     * Xử lý logic đăng xuất người dùng.
     */
    private void logoutUser() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.remove(KEY_LOGGED_IN_EMAIL);
        editor.apply();

        Toast.makeText(this, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
        updateUIBasedOnLoginStatus();
    }
}