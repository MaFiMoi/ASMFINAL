package com.example.asmfinal.Session;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.asmfinal.R;
import com.example.asmfinal.Session.LoginActivity;
import com.example.asmfinal.Session.CalendarActivity; // ;Import CalendarActivity
import com.example.asmfinal.adapter.Expense;
import com.example.asmfinal.adapter.ExpenseAdapter;
import com.example.asmfinal.database.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView tvInitialBalance, tvFinalBalance, tvTotalExpense;
    private FloatingActionButton fabAdd;
    private RecyclerView recyclerViewExpenses;
    private ExpenseAdapter expenseAdapter;
    private List<Expense> monthlyCategoryExpenses;

    // Custom Bottom Navigation components
    private LinearLayout navHomeLayout, navCalendarLayout, navChartLayout, navAccountLayout;
    private ImageView icHome, icCalendar, icChart, icAccount;
    private TextView tvHome, tvCalendar, tvChart, tvAccount;

    private static final int INITIAL_BALANCE = 30000000;
    private DecimalFormat formatter = new DecimalFormat("#,###");
    private Map<String, String> categoryNames = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeDatabase();
        initializeCategories();
        setupRecyclerView();
        setupClickListeners();
        setupCustomBottomNavigation();
        loadExpenseData();
    }

    private void initializeViews() {
        tvInitialBalance = findViewById(R.id.tvInitialBalance);
        tvFinalBalance = findViewById(R.id.tvFinalBalance);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        fabAdd = findViewById(R.id.fabAdd);
        recyclerViewExpenses = findViewById(R.id.recyclerViewExpenses);

        // Ánh xạ các thành phần của custom bottom navigation
        navHomeLayout = findViewById(R.id.nav_home_layout);
        navCalendarLayout = findViewById(R.id.nav_calendar_layout);
        navChartLayout = findViewById(R.id.nav_chart_layout);
        navAccountLayout = findViewById(R.id.nav_account_layout);

        icHome = findViewById(R.id.ic_home);
        icCalendar = findViewById(R.id.ic_calendar);
        icChart = findViewById(R.id.ic_chart);
        icAccount = findViewById(R.id.ic_account);

        tvHome = findViewById(R.id.tv_home);
        tvCalendar = findViewById(R.id.tv_calendar);
        tvChart = findViewById(R.id.tv_chart);
        tvAccount = findViewById(R.id.tv_account);
    }

    private void setupCustomBottomNavigation() {
        // Đặt màu mặc định cho tab Home (đã được chọn)
        setActiveTab(icHome, tvHome);

        navHomeLayout.setOnClickListener(v -> {
            setActiveTab(icHome, tvHome);
            Toast.makeText(this, "Bạn đang ở Trang Chủ", Toast.LENGTH_SHORT).show();
            // Không cần Intent vì đã ở MainActivity
        });

        navCalendarLayout.setOnClickListener(v -> {
            setActiveTab(icCalendar, tvCalendar);
            // Chuyển sang CalendarActivity
            Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
            startActivity(intent);
            // Có thể thêm finish() nếu không muốn quay lại MainActivity
            // finish();
        });

        navChartLayout.setOnClickListener(v -> {
            setActiveTab(icChart, tvChart);
            Toast.makeText(this, "Chức năng Phân Tích", Toast.LENGTH_SHORT).show();
            // TODO: Chuyển sang ChartActivity
            // Intent intent = new Intent(MainActivity.this, ChartActivity.class);
            // startActivity(intent);
        });

        navAccountLayout.setOnClickListener(v -> {
            setActiveTab(icAccount, tvAccount);
            Toast.makeText(this, "Chức năng Tài Khoản", Toast.LENGTH_SHORT).show();
            // TODO: Chuyển sang AccountActivity
            // Intent intent = new Intent(MainActivity.this, AccountActivity.class);
            // startActivity(intent);
        });
    }

    private void setActiveTab(ImageView activeIcon, TextView activeText) {
        // Reset tất cả các tab về màu inactive
        int inactiveColor = ContextCompat.getColor(this, R.color.inactive_tab_color);
        icHome.setColorFilter(inactiveColor);
        icCalendar.setColorFilter(inactiveColor);
        icChart.setColorFilter(inactiveColor);
        icAccount.setColorFilter(inactiveColor);

        tvHome.setTextColor(inactiveColor);
        tvCalendar.setTextColor(inactiveColor);
        tvChart.setTextColor(inactiveColor);
        tvAccount.setTextColor(inactiveColor);

        // Đặt màu active cho tab được chọn
        int activeColor = ContextCompat.getColor(this, R.color.active_tab_color);
        activeIcon.setColorFilter(activeColor);
        activeText.setTextColor(activeColor);
    }

    private void initializeDatabase() {
        dbHelper = new DatabaseHelper(this);
    }

    private void initializeCategories() {
        categoryNames.put("food", "Ăn uống");
        categoryNames.put("transport", "Di chuyển");
        categoryNames.put("water", "Tiền nước");
        categoryNames.put("phone", "Tiền điện thoại");
        categoryNames.put("electricity", "Tiền điện");
        categoryNames.put("maintenance", "Bảo dưỡng xe");
    }

    private void setupRecyclerView() {
        monthlyCategoryExpenses = new ArrayList<>();
        expenseAdapter = new ExpenseAdapter(monthlyCategoryExpenses);
        recyclerViewExpenses.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewExpenses.setAdapter(expenseAdapter);
    }

    private void setupClickListeners() {
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddExpenseDialog();
            }
        });
    }

    private void loadExpenseData() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

        List<Expense> expenses = dbHelper.getExpensesForMonth(startDate, endDate);
        Map<String, Integer> categoryTotals = new HashMap<>();
        int totalExpenses = 0;

        for (Expense expense : expenses) {
            String category = expense.getCategory() != null && categoryNames.containsKey(expense.getCategory())
                    ? expense.getCategory() : "food";
            int currentTotal = categoryTotals.getOrDefault(category, 0);
            categoryTotals.put(category, currentTotal + expense.getAmount());
            totalExpenses += expense.getAmount();
        }

        updateUI(categoryTotals, totalExpenses);
    }

    private void updateUI(Map<String, Integer> categoryTotals, int totalExpenses) {
        tvInitialBalance.setText(formatter.format(INITIAL_BALANCE) + " VND");
        int finalBalance = INITIAL_BALANCE - totalExpenses;
        tvFinalBalance.setText(formatter.format(finalBalance) + " VND");
        tvTotalExpense.setText("-" + formatter.format(totalExpenses) + " VND");

        monthlyCategoryExpenses.clear();
        for (Map.Entry<String, String> entry : categoryNames.entrySet()) {
            String categoryKey = entry.getKey();
            String categoryName = entry.getValue();
            int amount = categoryTotals.getOrDefault(categoryKey, 0);

            if (amount > 0) {
                monthlyCategoryExpenses.add(new Expense(categoryName, amount, categoryKey));
            }
        }
        expenseAdapter.notifyDataSetChanged();

        expenseAdapter.setOnItemClickListener(new ExpenseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Expense expense) {
                showExpenseDetails(expense.getDate(), expense.getTitle());
            }
        });
    }

    private void showAddExpenseDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_expense);
        dialog.getWindow().setLayout(
                getResources().getDisplayMetrics().widthPixels - 100,
                CardView.LayoutParams.WRAP_CONTENT
        );

        Spinner spinnerCategory = dialog.findViewById(R.id.spinnerCategory);
        EditText etAmount = dialog.findViewById(R.id.etAmount);
        EditText etDescription = dialog.findViewById(R.id.etDescription);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnSave = dialog.findViewById(R.id.btnSave);

        TextView txtDate = dialog.findViewById(R.id.txtDate);
        TextView txtTime = dialog.findViewById(R.id.txtTime);

        final Calendar selectedCalendar = Calendar.getInstance();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        txtDate.setText(dateFormat.format(selectedCalendar.getTime()));
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        txtTime.setText(timeFormat.format(selectedCalendar.getTime()));

        List<String> categories = new ArrayList<>();
        for (String categoryName : categoryNames.values()) {
            categories.add(categoryName);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        txtDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    MainActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        selectedCalendar.set(Calendar.YEAR, year);
                        selectedCalendar.set(Calendar.MONTH, month);
                        selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        txtDate.setText(dateFormat.format(selectedCalendar.getTime()));
                    },
                    selectedCalendar.get(Calendar.YEAR),
                    selectedCalendar.get(Calendar.MONTH),
                    selectedCalendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        txtTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    MainActivity.this,
                    (view, hourOfDay, minute) -> {
                        selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedCalendar.set(Calendar.MINUTE, minute);
                        txtTime.setText(timeFormat.format(selectedCalendar.getTime()));
                    },
                    selectedCalendar.get(Calendar.HOUR_OF_DAY),
                    selectedCalendar.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        });

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        btnSave.setOnClickListener(v -> {
            String selectedCategory = spinnerCategory.getSelectedItem().toString();
            String amountStr = etAmount.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (amountStr.isEmpty()) {
                Toast.makeText(MainActivity.this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int amount = Integer.parseInt(amountStr);
                if (amount <= 0) {
                    Toast.makeText(MainActivity.this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                String categoryKey = getCategoryKeyFromName(selectedCategory);
                String selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.getTime());
                saveExpense(categoryKey, amount, description, selectedDate);

                dialog.dismiss();
                loadExpenseData();
                Toast.makeText(MainActivity.this, "Đã thêm chi tiêu thành công", Toast.LENGTH_SHORT).show();

            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private String getCategoryKeyFromName(String categoryName) {
        for (Map.Entry<String, String> entry : categoryNames.entrySet()) {
            if (entry.getValue().equals(categoryName)) {
                return entry.getKey();
            }
        }
        return "food";
    }

    private void saveExpense(String category, int amount, String description, String dateString) {
        long result = dbHelper.insertExpenseWithCategory(description, amount, dateString, category);
        if (result == -1) {
            Toast.makeText(this, "Lỗi khi lưu dữ liệu", Toast.LENGTH_SHORT).show();
        }
    }

    private void showExpenseDetails(String categoryKey, String categoryName) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

        List<Expense> expenses = dbHelper.getExpensesByCategory(categoryKey, startDate, endDate);
        StringBuilder details = new StringBuilder();
        details.append(categoryName).append(":\n\n");

        int total = 0;
        for (Expense expense : expenses) {
            details.append("Ngày: ").append(expense.getDate()).append("\n");
            details.append("Số tiền: ").append(formatter.format(expense.getAmount())).append(" VND\n");
            if (expense.getTitle() != null && !expense.getTitle().isEmpty()) {
                details.append("Mô tả: ").append(expense.getTitle()).append("\n");
            }
            details.append("\n");
            total += expense.getAmount();
        }

        details.append("Tổng cộng: ").append(formatter.format(total)).append(" VND");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chi tiết chi tiêu")
                .setMessage(details.toString())
                .setPositiveButton("Đóng", null)
                .show();
    }
}