package com.example.asmfinal;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog; // Use AlertDialog from androidx
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager; // Import LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView; // Import RecyclerView

import com.example.asmfinal.adapter.Expense;
import com.example.asmfinal.adapter.ExpenseAdapter; // Import your ExpenseAdapter
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

    // RecyclerView components
    private RecyclerView recyclerViewExpenses;
    private ExpenseAdapter expenseAdapter;
    private List<Expense> monthlyCategoryExpenses; // List to hold summarized category expenses for the RecyclerView

    private static final int INITIAL_BALANCE = 30000000; // 30 million VND
    private DecimalFormat formatter = new DecimalFormat("#,###");

    // Expense categories with their names
    private Map<String, String> categoryNames = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeDatabase();
        initializeCategories();
        setupRecyclerView(); // Setup RecyclerView here
        setupClickListeners();
        loadExpenseData();
    }

    private void initializeViews() {
        tvInitialBalance = findViewById(R.id.tvInitialBalance);
        tvFinalBalance = findViewById(R.id.tvFinalBalance);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        fabAdd = findViewById(R.id.fabAdd);
        recyclerViewExpenses = findViewById(R.id.recyclerViewExpenses); // Get reference to RecyclerView
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
        monthlyCategoryExpenses = new ArrayList<>(); // Initialize the list for the adapter
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
        // Get current month date range
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

        // Get expenses for current month
        List<Expense> expenses = dbHelper.getExpensesForMonth(startDate, endDate);

        Map<String, Integer> categoryTotals = new HashMap<>();
        int totalExpenses = 0;

        // Calculate totals by category
        for (Expense expense : expenses) {
            // Ensure the category from DB matches keys in categoryNames map, default to "food" if null/unknown
            String category = expense.getCategory() != null && categoryNames.containsKey(expense.getCategory())
                    ? expense.getCategory() : "food";
            int currentTotal = categoryTotals.getOrDefault(category, 0);
            categoryTotals.put(category, currentTotal + expense.getAmount());
            totalExpenses += expense.getAmount();
        }

        updateUI(categoryTotals, totalExpenses);
    }

    private void updateUI(Map<String, Integer> categoryTotals, int totalExpenses) {
        // Update balance information
        tvInitialBalance.setText(formatter.format(INITIAL_BALANCE) + " VND");
        int finalBalance = INITIAL_BALANCE - totalExpenses;
        tvFinalBalance.setText(formatter.format(finalBalance) + " VND");
        tvTotalExpense.setText("-" + formatter.format(totalExpenses) + " VND");

        // Prepare data for RecyclerView
        monthlyCategoryExpenses.clear(); // Clear previous data
        for (Map.Entry<String, String> entry : categoryNames.entrySet()) {
            String categoryKey = entry.getKey();
            String categoryName = entry.getValue();
            int amount = categoryTotals.getOrDefault(categoryKey, 0);

            if (amount > 0) {
                // For RecyclerView, the 'date' can be empty or the current month,
                // as the RecyclerView items are now categories with total amounts for the month.
                // We'll pass the categoryKey in the description or title for details screen.
                monthlyCategoryExpenses.add(new Expense(categoryName, amount, categoryKey)); // Using categoryKey as 'date' temporarily for simplicity
            }
        }
        expenseAdapter.notifyDataSetChanged(); // Notify adapter of data change

        // Set up click listeners for the RecyclerView items
        expenseAdapter.setOnItemClickListener(new ExpenseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Expense expense) {
                // The 'date' field of the Expense object in monthlyCategoryExpenses list
                // is now storing the categoryKey.
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

        // Initialize dialog views
        Spinner spinnerCategory = dialog.findViewById(R.id.spinnerCategory);
        EditText etAmount = dialog.findViewById(R.id.etAmount);
        EditText etDescription = dialog.findViewById(R.id.etDescription); // Now an EditText
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnSave = dialog.findViewById(R.id.btnSave);

        // Date and Time TextViews
        TextView txtDate = dialog.findViewById(R.id.txtDate);
        TextView txtTime = dialog.findViewById(R.id.txtTime);

        // Get current date and time to pre-fill or use as starting point
        final Calendar selectedCalendar = Calendar.getInstance(); // Use a Calendar to store the selected date/time

        // Format and set initial date/time (current date/time)
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        txtDate.setText(dateFormat.format(selectedCalendar.getTime()));

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        txtTime.setText(timeFormat.format(selectedCalendar.getTime()));

        // Setup category spinner
        List<String> categories = new ArrayList<>();
        for (String categoryName : categoryNames.values()) {
            categories.add(categoryName);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Set up click listener for Date
        txtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });

        // Set up click listener for Time
        txtTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        MainActivity.this,
                        (view, hourOfDay, minute) -> {
                            selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            selectedCalendar.set(Calendar.MINUTE, minute);
                            txtTime.setText(timeFormat.format(selectedCalendar.getTime()));
                        },
                        selectedCalendar.get(Calendar.HOUR_OF_DAY),
                        selectedCalendar.get(Calendar.MINUTE),
                        true // true for 24-hour format, false for AM/PM
                );
                timePickerDialog.show();
            }
        });


        // Setup button listeners
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedCategory = spinnerCategory.getSelectedItem().toString();
                String amountStr = etAmount.getText().toString().trim();
                String description = etDescription.getText().toString().trim(); // Get text from EditText

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

                    // Get category key from name
                    String categoryKey = getCategoryKeyFromName(selectedCategory);

                    // Get the selected date from selectedCalendar for saving
                    String selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.getTime());

                    // Save to database with the selected date
                    saveExpense(categoryKey, amount, description, selectedDate);

                    dialog.dismiss();
                    loadExpenseData(); // Refresh the UI

                    Toast.makeText(MainActivity.this, "Đã thêm chi tiêu thành công", Toast.LENGTH_SHORT).show();

                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                }
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
        return "food"; // default
    }

    // Modified saveExpense to accept a dateString
    private void saveExpense(String category, int amount, String description, String dateString) {
        long result = dbHelper.insertExpenseWithCategory(description, amount, dateString, category);

        if (result == -1) {
            Toast.makeText(this, "Lỗi khi lưu dữ liệu", Toast.LENGTH_SHORT).show();
        }
    }

    private void showExpenseDetails(String categoryKey, String categoryName) {
        // Get current month date range
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

        // Get expenses for this category
        List<Expense> expenses = dbHelper.getExpensesByCategory(categoryKey, startDate, endDate);

        StringBuilder details = new StringBuilder();
        details.append(categoryName).append(":\n\n"); // Use categoryName directly for display

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

        // Show details dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chi tiết chi tiêu")
                .setMessage(details.toString())
                .setPositiveButton("Đóng", null)
                .show();
    }
}