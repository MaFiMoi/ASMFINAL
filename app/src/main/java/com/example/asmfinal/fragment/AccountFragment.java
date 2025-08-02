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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout cho Fragment này
        return inflater.inflate(R.layout.fragment_account, container, false);
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
        // Kiểm tra null trước khi gán listener để tránh NullPointerException
        if (menuAccount != null) {
            menuAccount.setOnClickListener(v -> Toast.makeText(requireContext(), "Bạn đã click Tài khoản", Toast.LENGTH_SHORT).show());
        }
        if (menuChangePassword != null) {
            menuChangePassword.setOnClickListener(v -> Toast.makeText(requireContext(), "Bạn đã click Đổi Mật khẩu", Toast.LENGTH_SHORT).show());
        }
        if (menuLanguage != null) {
            menuLanguage.setOnClickListener(v -> Toast.makeText(requireContext(), "Bạn đã click Ngôn ngữ", Toast.LENGTH_SHORT).show());
        }
        if (menuExportCSV != null) {
            menuExportCSV.setOnClickListener(v -> Toast.makeText(requireContext(), "Bạn đã click Xuất CSV", Toast.LENGTH_SHORT).show());
        }
        if (menuHistory != null) {
            menuHistory.setOnClickListener(v -> Toast.makeText(requireContext(), "Bạn đã click Lịch sử", Toast.LENGTH_SHORT).show());
        }
        if (menuExchangeRate != null) {
            menuExchangeRate.setOnClickListener(v -> Toast.makeText(requireContext(), "Bạn đã click Tỷ giá tiền tệ", Toast.LENGTH_SHORT).show());
        }

        if (switchDarkMode != null) {
            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SharedPreferences.Editor editor = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
                editor.putBoolean(KEY_DARK_MODE, isChecked);
                editor.apply();

                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    Toast.makeText(requireContext(), "Chế độ Tối đã bật", Toast.LENGTH_SHORT).show();
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    Toast.makeText(requireContext(), "Chế độ Tối đã tắt", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                loginLauncher.launch(intent);
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> logoutUser());
        }
    }

    /**
     * Cập nhật giao diện người dùng dựa trên trạng thái đăng nhập.
     */
    private void updateUIBasedOnLoginStatus() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String loggedInEmail = prefs.getString(KEY_LOGGED_IN_EMAIL, null);

        // Cập nhật trạng thái của Dark Mode Switch
        boolean isDarkModeEnabled = prefs.getBoolean(KEY_DARK_MODE, false);
        if (switchDarkMode != null) {
            switchDarkMode.setChecked(isDarkModeEnabled);
        }

        if (loggedInEmail != null) {
            // Đã đăng nhập
            User currentUser = databaseHelper.getUserByEmail(loggedInEmail);
            if (currentUser != null) {
                if (tvUserName != null) {
                    tvUserName.setText(currentUser.getFullName());
                }

                if (profileSection != null) profileSection.setVisibility(View.VISIBLE);
                if (noDataContainer != null) noDataContainer.setVisibility(View.GONE);
                if (btnLogout != null) btnLogout.setVisibility(View.VISIBLE);
                if (btnLogin != null) btnLogin.setVisibility(View.GONE);
            } else {
                // Trường hợp người dùng không còn tồn tại trong DB, tự động đăng xuất
                logoutUser();
            }
        } else {
            // Chưa đăng nhập
            if (tvUserName != null) tvUserName.setText("");

            if (profileSection != null) profileSection.setVisibility(View.GONE);
            if (noDataContainer != null) noDataContainer.setVisibility(View.VISIBLE);
            if (btnLogout != null) btnLogout.setVisibility(View.GONE);
            if (btnLogin != null) btnLogin.setVisibility(View.VISIBLE);
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