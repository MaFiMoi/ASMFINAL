package com.example.asmfinal.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.asmfinal.R;
import com.example.asmfinal.database.DatabaseHelper;
import com.example.asmfinal.model.User;
import com.example.asmfinal.Session.LoginActivity;

public class AccountFragment extends Fragment {

    // Khai báo các thành phần UI
    private RelativeLayout header;
    private LinearLayout profileSection;
    private LinearLayout noDataContainer;
    private LinearLayout menuAccount;
    private LinearLayout menuChangePassword;
    private LinearLayout menuLanguage;
    private LinearLayout menuExportCSV;
    private LinearLayout menuHistory;
    private LinearLayout menuExchangeRate;
    private Switch switchDarkMode;
    private Button btnLogin;
    private LinearLayout btnLogout;
    private TextView tvUserName;

    // Instance của DatabaseHelper
    private DatabaseHelper databaseHelper;

    // Tên file SharedPreferences và key
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String KEY_DARK_MODE = "isDarkModeEnabled";
    private static final String KEY_LOGGED_IN_EMAIL = "loggedInUserEmail";

    // Khai báo ActivityResultLauncher
    private final ActivityResultLauncher<Intent> loginLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == requireActivity().RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.hasExtra("loggedInEmail")) {
                        String email = data.getStringExtra("loggedInEmail");
                        SharedPreferences.Editor editor = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
                        editor.putString(KEY_LOGGED_IN_EMAIL, email);
                        editor.apply();

                        Toast.makeText(requireContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        updateUIBasedOnLoginStatus();
                    }
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Áp dụng Dark Mode ở đây hoặc trong Activity chứa Fragment
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout cho Fragment này
        // Đã sửa để sử dụng layout fragment_account
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo DatabaseHelper
        databaseHelper = new DatabaseHelper(requireContext());

        // Khởi tạo views và thiết lập các sự kiện
        initViews(view);
        setupListeners();
        updateUIBasedOnLoginStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cập nhật UI mỗi khi Fragment được resumed
        updateUIBasedOnLoginStatus();
    }

    /**
     * Khởi tạo các thành phần UI bằng cách tìm ID từ layout.
     * @param view Root view của Fragment
     */
    private void initViews(View view) {
        header = view.findViewById(R.id.header);
        profileSection = view.findViewById(R.id.profileSection);
        noDataContainer = view.findViewById(R.id.noDataContainer);

        menuAccount = view.findViewById(R.id.menuAccount);
        menuChangePassword = view.findViewById(R.id.menuChangePassword);
        menuLanguage = view.findViewById(R.id.menuLanguage);
        menuExportCSV = view.findViewById(R.id.menuExportCSV);
        menuHistory = view.findViewById(R.id.menuHistory);
        menuExchangeRate = view.findViewById(R.id.menuExchangeRate);

        switchDarkMode = view.findViewById(R.id.switchDarkMode);

        btnLogin = view.findViewById(R.id.btnLogin);
        btnLogout = view.findViewById(R.id.btnLogout);

        tvUserName = view.findViewById(R.id.tvUserName);
    }

    /**
     * Thiết lập các sự kiện click và thay đổi trạng thái cho các thành phần UI.
     */
    private void setupListeners() {
        menuAccount.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Bạn đã click Tài khoản", Toast.LENGTH_SHORT).show();
            // TODO: Mở màn hình chi tiết tài khoản
        });

        menuChangePassword.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Bạn đã click Đổi Mật khẩu", Toast.LENGTH_SHORT).show();
            // TODO: Mở màn hình đổi mật khẩu
        });

        menuLanguage.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Bạn đã click Ngôn ngữ", Toast.LENGTH_SHORT).show();
            // TODO: Hiển thị dialog chọn ngôn ngữ
        });

        menuExportCSV.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Bạn đã click Xuất CSV", Toast.LENGTH_SHORT).show();
            // TODO: Xử lý logic xuất file CSV
        });

        menuHistory.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Bạn đã click Lịch sử", Toast.LENGTH_SHORT).show();
            // TODO: Mở màn hình lịch sử
        });

        menuExchangeRate.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Bạn đã click Tỷ giá tiền tệ", Toast.LENGTH_SHORT).show();
            // TODO: Mở màn hình tỷ giá tiền tệ
        });

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
            editor.putBoolean(KEY_DARK_MODE, isChecked);
            editor.apply();

            // Set chế độ tối cho ứng dụng
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Toast.makeText(requireContext(), "Chế độ Tối đã bật", Toast.LENGTH_SHORT).show();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Toast.makeText(requireContext(), "Chế độ Tối đã tắt", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogin.setOnClickListener(v -> {
            // Mở màn hình đăng nhập
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            loginLauncher.launch(intent);
        });

        btnLogout.setOnClickListener(v -> {
            logoutUser();
        });
    }

    /**
     * Cập nhật giao diện người dùng dựa trên trạng thái đăng nhập.
     */
    private void updateUIBasedOnLoginStatus() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String loggedInEmail = prefs.getString(KEY_LOGGED_IN_EMAIL, null);

        // Cập nhật trạng thái của Dark Mode Switch
        boolean isDarkModeEnabled = prefs.getBoolean(KEY_DARK_MODE, false);
        switchDarkMode.setChecked(isDarkModeEnabled);

        if (loggedInEmail != null) {
            // Đã đăng nhập
            User currentUser = databaseHelper.getUserByEmail(loggedInEmail);
            if (currentUser != null) {
                tvUserName.setText(currentUser.getFullName());

                profileSection.setVisibility(View.VISIBLE);
                noDataContainer.setVisibility(View.GONE);
                btnLogout.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.GONE); // Ẩn nút đăng nhập
            } else {
                // Trường hợp người dùng không còn tồn tại trong DB, tự động đăng xuất
                logoutUser();
            }
        } else {
            // Chưa đăng nhập
            tvUserName.setText("");

            profileSection.setVisibility(View.GONE);
            noDataContainer.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE); // Hiển thị nút đăng nhập
        }
    }

    /**
     * Xử lý logic đăng xuất người dùng.
     */
    private void logoutUser() {
        SharedPreferences.Editor editor = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.remove(KEY_LOGGED_IN_EMAIL);
        editor.apply();

        Toast.makeText(requireContext(), "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
        updateUIBasedOnLoginStatus();
    }
}