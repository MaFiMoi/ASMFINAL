package com.example.asmfinal.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarFragment extends Fragment {

    private Calendar currentCalendar;
    private GridLayout calendarGrid;
    private TextView tvCurrentMonth, tvTotalIncome, tvTotalExpense, tvTotal;
    private ImageButton btnPreviousMonth, btnNextMonth;
    private RecyclerView recyclerViewTransactions;
    private ExpenseAdapter transactionAdapter;
    private FloatingActionButton fabAdd;
    private TextView tvNoTransactions;

    private SharedViewModel sharedViewModel;
    private List<Expense> currentMonthExpenses;
    private List<Expense> displayedExpenses;
    private DecimalFormat currencyFormat;
    private SimpleDateFormat monthYearFormat;
    private SimpleDateFormat dbDateFormat;
    private DatabaseHelper databaseHelper;
    private View lastSelectedDayView = null;
    private final Locale vietnameseLocale = new Locale("vi", "VN");
    private int selectedDay = -1;

    private Map<String, String> categoryNames = new HashMap<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        initializeCategoryNames();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        initViews(view);
        initData();
        setupRecyclerView();
        setupClickListeners();

        sharedViewModel.getExpenses().observe(getViewLifecycleOwner(), expenses -> {
            Calendar now = Calendar.getInstance();
            if (currentCalendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                    currentCalendar.get(Calendar.MONTH) == now.get(Calendar.MONTH)) {
                Log.d("CalendarFragment", "Dữ liệu chi tiêu đã thay đổi, cập nhật lịch tháng hiện tại");
                currentMonthExpenses = new ArrayList<>(expenses);
                setCategoryNames(currentMonthExpenses);
                updateCalendarDisplay();
                if (selectedDay == -1) {
                    showAllMonthExpenses();
                } else {
                    showExpensesForDay(selectedDay);
                }
            }
        });

        loadDataForCurrentMonth();

        return view;
    }

    private void initializeCategoryNames() {
        categoryNames.put("food", "Ăn uống");
        categoryNames.put("transport", "Di chuyển");
        categoryNames.put("water", "Tiền nước");
        categoryNames.put("phone", "Tiền điện thoại");
        categoryNames.put("electricity", "Tiền điện");
        categoryNames.put("maintenance", "Bảo dưỡng xe");
    }

    private void setCategoryNames(List<Expense> expenses) {
        for (Expense expense : expenses) {
            if (expense.getCategoryName() == null || expense.getCategoryName().isEmpty()) {
                expense.setCategoryName(categoryNames.getOrDefault(expense.getCategory(), "Không xác định"));
            }
        }
    }

    private void initViews(View view) {
        calendarGrid = view.findViewById(R.id.calendarGrid);
        tvCurrentMonth = view.findViewById(R.id.tvCurrentMonth);
        tvTotalIncome = view.findViewById(R.id.tvTotalIncome);
        tvTotalExpense = view.findViewById(R.id.tvTotalExpense);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        recyclerViewTransactions = view.findViewById(R.id.recyclerViewTransactions);
        fabAdd = view.findViewById(R.id.fabAdd);
        tvNoTransactions = view.findViewById(R.id.tvNoTransactions);
    }

    private void setupClickListeners() {
        btnPreviousMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            selectedDay = -1;
            loadDataForCurrentMonth();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            selectedDay = -1;
            loadDataForCurrentMonth();
        });

        fabAdd.setOnClickListener(v -> showAddExpenseDialog());
    }

    private void initData() {
        databaseHelper = new DatabaseHelper(requireContext());
        currentCalendar = Calendar.getInstance();
        currencyFormat = new DecimalFormat("#,###");
        monthYearFormat = new SimpleDateFormat("MMMM yyyy", vietnameseLocale);
        dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        currentMonthExpenses = new ArrayList<>();
        displayedExpenses = new ArrayList<>();
    }

    private void setupRecyclerView() {
        transactionAdapter = new ExpenseAdapter(displayedExpenses);
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewTransactions.setAdapter(transactionAdapter);

        transactionAdapter.setOnItemClickListener(this::showExpenseDetailDialog);
    }

    private void loadDataForCurrentMonth() {
        Calendar monthStart = (Calendar) currentCalendar.clone();
        monthStart.set(Calendar.DAY_OF_MONTH, 1);
        Calendar monthEnd = (Calendar) currentCalendar.clone();
        monthEnd.set(Calendar.DAY_OF_MONTH, monthEnd.getActualMaximum(Calendar.DAY_OF_MONTH));

        String startDate = dbDateFormat.format(monthStart.getTime());
        String endDate = dbDateFormat.format(monthEnd.getTime());

        Log.d("CalendarFragment", "Loading data for month: " + startDate + " to " + endDate);

        currentMonthExpenses = databaseHelper.getExpensesForMonth(startDate, endDate);
        setCategoryNames(currentMonthExpenses);

        updateCalendarDisplay();

        showAllMonthExpenses();
    }

    private void updateCalendarDisplay() {
        tvCurrentMonth.setText(monthYearFormat.format(currentCalendar.getTime()));
        calendarGrid.removeAllViews();

        lastSelectedDayView = null;

        Calendar tempCal = (Calendar) currentCalendar.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int startDay = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : firstDayOfWeek - 2;

        for (int i = 0; i < startDay; i++) {
            addEmptyDayView();
        }

        for (int day = 1; day <= daysInMonth; day++) {
            addDayView(day);
        }

        updateMonthlySummary();
    }

    private void addEmptyDayView() {
        View emptyView = LayoutInflater.from(requireContext()).inflate(R.layout.calendar_day_empty, calendarGrid, false);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = 60;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(1, 1, 1, 1);
        emptyView.setLayoutParams(params);
        calendarGrid.addView(emptyView);
    }

    private void addDayView(int day) {
        View dayView = LayoutInflater.from(requireContext()).inflate(R.layout.calendar_day_item, calendarGrid, false);
        TextView tvDay = dayView.findViewById(R.id.tvDay);
        TextView tvAmount = dayView.findViewById(R.id.tvAmount);
        View indicatorIncome = dayView.findViewById(R.id.indicatorIncome);
        View indicatorExpense = dayView.findViewById(R.id.indicatorExpense);

        tvDay.setText(String.valueOf(day));

        Calendar today = Calendar.getInstance();
        boolean isToday = (currentCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                currentCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                day == today.get(Calendar.DAY_OF_MONTH));

        if (isToday) {
            tvDay.setBackgroundResource(R.drawable.calendar_day_today_background);
            tvDay.setTextColor(getResources().getColor(android.R.color.white, null));
            tvDay.setPadding(8, 8, 8, 8);
        } else {
            tvDay.setBackground(null);
            tvDay.setTextColor(getResources().getColor(R.color.black, null));
            tvDay.setPadding(0, 0, 0, 0);
        }

        List<Expense> dayExpenses = getExpensesForDay(day);
        double totalIncome = 0;
        double totalExpense = 0;

        for (Expense expense : dayExpenses) {
            if (expense.getAmount() >= 0) {
                totalIncome += expense.getAmount();
            } else {
                totalExpense += Math.abs(expense.getAmount());
            }
        }

        if (totalIncome > 0 || totalExpense > 0) {
            indicatorIncome.setVisibility(totalIncome > 0 ? View.VISIBLE : View.GONE);
            indicatorExpense.setVisibility(totalExpense > 0 ? View.VISIBLE : View.GONE);
            double netAmount = totalIncome - totalExpense;
            if (Math.abs(netAmount) >= 1) {
                tvAmount.setText(formatCurrency(netAmount));
                tvAmount.setVisibility(View.VISIBLE);
                tvAmount.setTextColor(netAmount >= 0 ?
                        getResources().getColor(R.color.green_positive, null) :
                        getResources().getColor(R.color.red_negative, null));
            } else {
                tvAmount.setVisibility(View.GONE);
            }
        } else {
            indicatorIncome.setVisibility(View.GONE);
            indicatorExpense.setVisibility(View.GONE);
            tvAmount.setVisibility(View.GONE);
        }

        dayView.setOnClickListener(v -> {
            selectedDay = day;
            showExpensesForDay(day);
            highlightSelectedDay(dayView);
        });

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = 60;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(1, 1, 1, 1);
        dayView.setLayoutParams(params);
        calendarGrid.addView(dayView);
    }

    private void showAllMonthExpenses() {
        selectedDay = -1;
        displayedExpenses.clear();
        displayedExpenses.addAll(currentMonthExpenses);
        transactionAdapter.notifyDataSetChanged();
        updateNoTransactionsVisibility();

        if (lastSelectedDayView != null) {
            lastSelectedDayView.setBackgroundResource(R.drawable.calendar_day_background);
            lastSelectedDayView = null;
        }
    }

    private void showExpensesForDay(int day) {
        displayedExpenses.clear();
        displayedExpenses.addAll(getExpensesForDay(day));
        transactionAdapter.notifyDataSetChanged();
        updateNoTransactionsVisibility();
    }

    private void highlightSelectedDay(View selectedDayView) {
        if (lastSelectedDayView != null) {
            lastSelectedDayView.setBackgroundResource(R.drawable.calendar_day_background);
        }
        selectedDayView.setBackgroundResource(R.drawable.calendar_day_selected_background);
        lastSelectedDayView = selectedDayView;
    }

    private List<Expense> getExpensesForDay(int day) {
        List<Expense> dayExpenses = new ArrayList<>();
        Calendar dayCalendar = (Calendar) currentCalendar.clone();
        dayCalendar.set(Calendar.DAY_OF_MONTH, day);
        String dayString = dbDateFormat.format(dayCalendar.getTime());

        for (Expense expense : currentMonthExpenses) {
            if (expense.getDate().equals(dayString)) {
                dayExpenses.add(expense);
            }
        }
        return dayExpenses;
    }

    private void updateMonthlySummary() {
        double totalIncome = 0;
        double totalExpense = 0;

        for (Expense expense : currentMonthExpenses) {
            if (expense.getAmount() >= 0) {
                totalIncome += expense.getAmount();
            } else {
                totalExpense += Math.abs(expense.getAmount());
            }
        }

        double netTotal = totalIncome - totalExpense;

        tvTotalIncome.setText(formatCurrency(totalIncome) + " VND");
        tvTotalExpense.setText("-" + formatCurrency(totalExpense) + " VND");
        tvTotal.setText(formatCurrency(netTotal) + " VND");

        tvTotalIncome.setTextColor(getResources().getColor(R.color.green_positive, null));
        tvTotalExpense.setTextColor(getResources().getColor(R.color.red_negative, null));
        tvTotal.setTextColor(netTotal >= 0 ?
                getResources().getColor(R.color.green_positive, null) :
                getResources().getColor(R.color.red_negative, null));
    }

    private void updateNoTransactionsVisibility() {
        if (displayedExpenses.isEmpty()) {
            tvNoTransactions.setVisibility(View.VISIBLE);
            recyclerViewTransactions.setVisibility(View.GONE);
        } else {
            tvNoTransactions.setVisibility(View.GONE);
            recyclerViewTransactions.setVisibility(View.VISIBLE);
        }
    }

    private String formatCurrency(double amount) {
        return currencyFormat.format(Math.abs(amount));
    }

    private void showAddExpenseDialog() {
        showExpenseDialog(null);
    }

    private void showExpenseDetailDialog(Expense expense) {
        Expense fullExpense = databaseHelper.getExpenseById(expense.getId());
        if (fullExpense != null) {
            fullExpense.setCategoryName(categoryNames.getOrDefault(fullExpense.getCategory(), "Không xác định"));

            Dialog dialog = new Dialog(requireContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_expense_detail);
            dialog.getWindow().setLayout(
                    getResources().getDisplayMetrics().widthPixels - 100,
                    CardView.LayoutParams.WRAP_CONTENT
            );

            TextView tvTitle = dialog.findViewById(R.id.tvDetailTitle);
            TextView tvCategory = dialog.findViewById(R.id.tvDetailCategory);
            TextView tvAmount = dialog.findViewById(R.id.tvDetailAmount);
            TextView tvDescription = dialog.findViewById(R.id.tvDetailDescription);
            TextView tvDate = dialog.findViewById(R.id.tvDetailDate);
            TextView tvTime = dialog.findViewById(R.id.tvDetailTime);
            Button btnClose = dialog.findViewById(R.id.btnCloseDetail);

            tvTitle.setText("Chi Tiết Chi Tiêu");

            tvCategory.setText("Danh mục: " + fullExpense.getCategoryName());

            String amountText = (fullExpense.getAmount() < 0 ? "-" : "+") +
                    formatCurrency(Math.abs(fullExpense.getAmount())) + " VND";
            tvAmount.setText(amountText);
            tvAmount.setTextColor(fullExpense.getAmount() < 0 ?
                    getResources().getColor(R.color.red_negative, null) :
                    getResources().getColor(R.color.green_positive, null));

            tvDescription.setText("Mô tả: " + (fullExpense.getDescription().isEmpty() ?
                    "Không có mô tả" : fullExpense.getDescription()));

            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = inputFormat.parse(fullExpense.getDate());
                tvDate.setText("Ngày: " + outputFormat.format(date));
            } catch (Exception e) {
                tvDate.setText("Ngày: " + fullExpense.getDate());
            }

            tvTime.setText("Thời gian: " + fullExpense.getTime());

            btnClose.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        } else {
            Toast.makeText(getContext(), "Không tìm thấy thông tin chi tiêu", Toast.LENGTH_SHORT).show();
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
                Log.e("CalendarFragment", "Lỗi phân tích ngày/giờ: " + e.getMessage());
            }

            int spinnerPosition = adapter.getPosition(categoryNames.get(expenseToEdit.getCategory()));
            spinnerCategory.setSelection(spinnerPosition);

            ivDelete.setVisibility(View.VISIBLE);
            ivDelete.setOnClickListener(v -> {
                databaseHelper.deleteExpense(expenseToEdit.getId());
                Toast.makeText(getContext(), "Đã xóa chi tiêu", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadDataForCurrentMonth();
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
                    databaseHelper.updateExpense(expenseToEdit.getId(), categoryKey, description, amount, selectedDate, selectedTime);
                    Toast.makeText(getContext(), "Đã cập nhật chi tiêu thành công", Toast.LENGTH_SHORT).show();
                } else {
                    databaseHelper.insertExpense(description, amount, selectedDate, selectedTime, categoryKey);
                    Toast.makeText(getContext(), "Đã thêm chi tiêu thành công", Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
                loadDataForCurrentMonth();
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