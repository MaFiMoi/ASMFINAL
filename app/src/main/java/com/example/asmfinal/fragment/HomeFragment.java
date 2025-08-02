package com.example.asmfinal.fragment;

import android.animation.ObjectAnimator;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.asmfinal.R;
import com.example.asmfinal.adapter.Expense;
import com.example.asmfinal.adapter.ExpenseAdapter;
import com.example.asmfinal.database.DatabaseHelper;
import com.example.asmfinal.model.SharedViewModel;
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
    private List<Expense> expensesList;
    private TextView tvExpenseListHeader;

    private SharedViewModel sharedViewModel;

    private DecimalFormat formatter = new DecimalFormat("#,###");
    private Map<String, String> categoryNames = new HashMap<>();

    private static final String PREF_NAME = "MyFinancePrefs";
    private static final String INITIAL_BALANCE_KEY = "initialBalance";
    private static final int DEFAULT_INITIAL_BALANCE = 30000000;

    private TextView tabPrevious, tabCurrent, tabNext;
    private View tabIndicator;
    private Calendar currentCalendar;
    private SimpleDateFormat monthYearFormat;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

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

        currentCalendar = Calendar.getInstance();
        monthYearFormat = new SimpleDateFormat("MMMM yyyy", new Locale("vi", "VN"));

        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Fragment đã được gắn vào context tại đây, an toàn để gọi
        updateTabUI(0);
    }


    private void initializeViews(View view) {
        tvInitialBalance = view.findViewById(R.id.tvInitialBalance);
        tvFinalBalance = view.findViewById(R.id.tvFinalBalance);
        tvTotalExpense = view.findViewById(R.id.tvTotalExpense);
        fabAdd = view.findViewById(R.id.fabAdd);
        recyclerViewExpenses = view.findViewById(R.id.recyclerViewExpenses);

        tabPrevious = view.findViewById(R.id.tabPrevious);
        tabCurrent = view.findViewById(R.id.tabCurrent);
        tabNext = view.findViewById(R.id.tabNext);
        tabIndicator = view.findViewById(R.id.tabIndicator);

        tvExpenseListHeader = view.findViewById(R.id.tvExpenseListHeader);
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
        expensesList = new ArrayList<>();
        expenseAdapter = new ExpenseAdapter(expensesList);
        recyclerViewExpenses.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewExpenses.setAdapter(expenseAdapter);

        expenseAdapter.setOnItemClickListener(this::showEditExpenseDialog);

        sharedViewModel.getExpenses().observe(getViewLifecycleOwner(), newExpenses -> {
            expensesList.clear();
            expensesList.addAll(newExpenses);
            for (Expense expense : expensesList) {
                if (expense.getCategoryName() == null || expense.getCategoryName().isEmpty()) {
                    expense.setCategoryName(categoryNames.getOrDefault(expense.getCategory(), "Không xác định"));
                }
            }
            expenseAdapter.notifyDataSetChanged();

            int totalExpenses = 0;
            for (Expense expense : newExpenses) {
                // Giả định chi tiêu là số âm, thu nhập là số dương
                if (expense.getAmount() < 0) {
                    totalExpenses += Math.abs(expense.getAmount());
                }
            }
            updateHomeUI(totalExpenses);
        });
    }

    private void setupClickListeners() {
        fabAdd.setOnClickListener(v -> showAddExpenseDialog());
        tvInitialBalance.setOnClickListener(v -> showEditInitialBalanceDialog());

        tabPrevious.setOnClickListener(v -> updateTabUI(-1));
        tabCurrent.setOnClickListener(v -> updateTabUI(0));
        tabNext.setOnClickListener(v -> updateTabUI(1));
    }

    private void updateTabUI(int offset) {
        if (!isAdded()) return; // ✅ Bảo vệ tránh crash

        currentCalendar = Calendar.getInstance();
        currentCalendar.add(Calendar.MONTH, offset);

        int activeColor = requireContext().getColor(R.color.black);
        int inactiveColor = requireContext().getColor(R.color.tab_inactive_color);

        tabPrevious.setTextColor(inactiveColor);
        tabCurrent.setTextColor(inactiveColor);
        tabNext.setTextColor(inactiveColor);

        if (offset == -1) {
            tabPrevious.setTextColor(activeColor);
        } else if (offset == 0) {
            tabCurrent.setTextColor(activeColor);
        } else {
            tabNext.setTextColor(activeColor);
        }

        updateTabIndicator(offset);
        loadExpenseData();
    }

    private void updateTabIndicator(int offset) {
        tabCurrent.post(() -> {
            // Get the width of the "Tháng này" tab
            int tabWidth = tabCurrent.getWidth();
            if (tabWidth == 0) {
                // If views haven't been drawn yet, set a listener to update later
                return;
            }

            // Calculate the translation position (translationX)
            float translationX = 0f;
            if (offset == -1) { // Previous month (Tháng trước)
                translationX = -tabWidth;
            } else if (offset == 0) { // Current month (Tháng này)
                translationX = 0;
            } else if (offset == 1) { // Next month (Tháng sau)
                translationX = tabWidth;
            }

            // Move the indicator line to the calculated position
            ObjectAnimator animator = ObjectAnimator.ofFloat(tabIndicator, "translationX", translationX);
            animator.setDuration(300); // 300ms
            animator.start();
        });
    }

    private int getInitialBalance() {
        SharedPreferences sharedPref = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPref.getInt(INITIAL_BALANCE_KEY, DEFAULT_INITIAL_BALANCE);
    }

    private void saveInitialBalance(int balance) {
        SharedPreferences sharedPref = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(INITIAL_BALANCE_KEY, balance);
        editor.apply();
    }

    private void loadExpenseData() {
        Calendar calendarStart = (Calendar) currentCalendar.clone();
        calendarStart.set(Calendar.DAY_OF_MONTH, 1);
        String startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendarStart.getTime());

        Calendar calendarEnd = (Calendar) currentCalendar.clone();
        calendarEnd.set(Calendar.DAY_OF_MONTH, calendarEnd.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendarEnd.getTime());

        List<Expense> allMonthlyExpenses = dbHelper.getExpensesForMonth(startDate, endDate);

        for (Expense expense : allMonthlyExpenses) {
            expense.setCategoryName(categoryNames.getOrDefault(expense.getCategory(), "Không xác định"));
        }

        SimpleDateFormat listHeaderFormat = new SimpleDateFormat("MMMM", new Locale("vi", "VN"));
        String monthName = listHeaderFormat.format(currentCalendar.getTime());
        if (monthName != null && !monthName.isEmpty()) {
            monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1).toLowerCase();
        }

        if (tvExpenseListHeader != null) {
            tvExpenseListHeader.setText("Danh sách chi tiêu tháng " + monthName);
        }

        sharedViewModel.setExpenses(allMonthlyExpenses);
    }

    private void updateHomeUI(int totalExpenses) {
        int initialBalance = getInitialBalance();
        tvInitialBalance.setText(formatter.format(initialBalance) + " VND");
        int finalBalance = initialBalance - totalExpenses;
        tvFinalBalance.setText(formatter.format(finalBalance) + " VND");
        tvTotalExpense.setText("-" + formatter.format(totalExpenses) + " VND");
    }

    private void showEditInitialBalanceDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_balance);
        dialog.getWindow().setLayout(
                getResources().getDisplayMetrics().widthPixels - 100,
                CardView.LayoutParams.WRAP_CONTENT
        );

        EditText etNewBalance = dialog.findViewById(R.id.etNewBalance);
        Button btnSave = dialog.findViewById(R.id.btnSaveBalance);
        Button btnCancel = dialog.findViewById(R.id.btnCancelBalance);

        etNewBalance.setText(String.valueOf(getInitialBalance()));

        btnSave.setOnClickListener(v -> {
            String newBalanceStr = etNewBalance.getText().toString().trim();
            if (!newBalanceStr.isEmpty()) {
                try {
                    int newBalance = Integer.parseInt(newBalanceStr);
                    if (newBalance < 0) {
                        Toast.makeText(getContext(), "Số tiền không được âm", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    saveInitialBalance(newBalance);
                    loadExpenseData();
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Số dư đã được cập nhật", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showAddExpenseDialog() {
        showExpenseDialog(null);
    }

    private void showEditExpenseDialog(Expense expense) {
        Expense fullExpense = dbHelper.getExpenseById(expense.getId());
        if (fullExpense != null) {
            fullExpense.setCategoryName(categoryNames.getOrDefault(fullExpense.getCategory(), "Không xác định"));
            showExpenseDialog(fullExpense);
        } else {
            Toast.makeText(getContext(), "Không tìm thấy chi tiêu để chỉnh sửa", Toast.LENGTH_SHORT).show();
        }
    }

    private void showExpenseDialog(@Nullable final Expense expenseToEdit) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_expense);
        dialog.getWindow().setLayout(
                getResources().getDisplayMetrics().widthPixels - 100,
                CardView.LayoutParams.WRAP_CONTENT
        );

        TextView edtTitle = dialog.findViewById(R.id.edtTitle);
        Spinner spinnerCategory = dialog.findViewById(R.id.spinnerCategory);
        EditText etAmount = dialog.findViewById(R.id.etAmount);
        EditText etDescription = dialog.findViewById(R.id.etDescription);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnSave = dialog.findViewById(R.id.btnSave);
        TextView txtDate = dialog.findViewById(R.id.txtDate);
        TextView txtTime = dialog.findViewById(R.id.txtTime);
        ImageView ivDelete = dialog.findViewById(R.id.ivDelete);

        final Calendar selectedCalendar = Calendar.getInstance();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>(categoryNames.values()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        if (expenseToEdit != null) {
            edtTitle.setText("Chỉnh Sửa Chi Tiêu");
            etAmount.setText(String.valueOf(Math.abs(expenseToEdit.getAmount())));
            etDescription.setText(expenseToEdit.getDescription());

            try {
                Date date = dateFormat.parse(expenseToEdit.getDate());
                Date time = timeFormat.parse(expenseToEdit.getTime());

                selectedCalendar.setTime(date);
                selectedCalendar.set(Calendar.HOUR_OF_DAY, time.getHours());
                selectedCalendar.set(Calendar.MINUTE, time.getMinutes());

                txtDate.setText(dateFormat.format(selectedCalendar.getTime()));
                txtTime.setText(timeFormat.format(selectedCalendar.getTime()));
            } catch (Exception e) {
                Log.e("HomeFragment", "Lỗi phân tích ngày/giờ: " + e.getMessage());
            }

            int spinnerPosition = adapter.getPosition(categoryNames.get(expenseToEdit.getCategory()));
            spinnerCategory.setSelection(spinnerPosition);

            ivDelete.setVisibility(View.VISIBLE);
            ivDelete.setOnClickListener(v -> {
                dbHelper.deleteExpense(expenseToEdit.getId());
                Toast.makeText(getContext(), "Đã xóa chi tiêu", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadExpenseData();
            });

        } else {
            edtTitle.setText("Thêm Chi Tiêu");
            txtDate.setText(dateFormat.format(selectedCalendar.getTime()));
            txtTime.setText(timeFormat.format(selectedCalendar.getTime()));
            ivDelete.setVisibility(View.GONE);
        }

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
                amount = -amount;

                String categoryKey = getCategoryKeyFromName(selectedCategory);
                String selectedDate = dateFormat.format(selectedCalendar.getTime());
                String selectedTime = timeFormat.format(selectedCalendar.getTime());

                if (expenseToEdit != null) {
                    dbHelper.updateExpense(expenseToEdit.getId(), categoryKey, description, amount, selectedDate, selectedTime);
                    Toast.makeText(getContext(), "Đã cập nhật chi tiêu thành công", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.insertExpense(description, amount, selectedDate, selectedTime, categoryKey);
                    Toast.makeText(getContext(), "Đã thêm chi tiêu thành công", Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
                loadExpenseData();
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
}