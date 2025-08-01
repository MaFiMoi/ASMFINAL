package com.example.asmfinal.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.asmfinal.R;
import com.example.asmfinal.adapter.Expense;
import com.example.asmfinal.adapter.ExpenseAdapter;
import com.example.asmfinal.database.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class HomeFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private TextView tvInitialBalance, tvFinalBalance, tvTotalExpense;
    private FloatingActionButton fabAdd;
    private RecyclerView recyclerViewExpenses;
    private ExpenseAdapter expenseAdapter;
    private List<Expense> monthlyCategoryExpenses;

    private static final int INITIAL_BALANCE = 30000000;
    private DecimalFormat formatter = new DecimalFormat("#,###");
    private Map<String, String> categoryNames = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initializeViews(view);
        initializeDatabase();
        initializeCategories();
        setupRecyclerView();
        setupClickListeners();
        loadExpenseData();
        return view;
    }

    private void initializeViews(View view) {
        tvInitialBalance = view.findViewById(R.id.tvInitialBalance);
        tvFinalBalance = view.findViewById(R.id.tvFinalBalance);
        tvTotalExpense = view.findViewById(R.id.tvTotalExpense);
        fabAdd = view.findViewById(R.id.fabAdd);
        recyclerViewExpenses = view.findViewById(R.id.recyclerViewExpenses);
    }

    private void initializeDatabase() {
        dbHelper = new DatabaseHelper(requireContext());
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
        recyclerViewExpenses.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewExpenses.setAdapter(expenseAdapter);
    }

    private void setupClickListeners() {
        fabAdd.setOnClickListener(v -> showAddExpenseDialog());
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
    }

    private void showAddExpenseDialog() {
        Dialog dialog = new Dialog(requireContext());
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
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        txtDate.setText(dateFormat.format(selectedCalendar.getTime()));
        txtTime.setText(timeFormat.format(selectedCalendar.getTime()));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>(categoryNames.values()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        txtDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
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
                    requireContext(),
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

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String selectedCategory = spinnerCategory.getSelectedItem().toString();
            String amountStr = etAmount.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int amount = Integer.parseInt(amountStr);
                if (amount <= 0) {
                    Toast.makeText(getContext(), "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                String categoryKey = getCategoryKeyFromName(selectedCategory);
                String selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.getTime());
                saveExpense(categoryKey, amount, description, selectedDate);

                dialog.dismiss();
                loadExpenseData();
                Toast.makeText(getContext(), "Đã thêm chi tiêu thành công", Toast.LENGTH_SHORT).show();

            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getContext(), "Lỗi khi lưu dữ liệu", Toast.LENGTH_SHORT).show();
        }
    }
}
