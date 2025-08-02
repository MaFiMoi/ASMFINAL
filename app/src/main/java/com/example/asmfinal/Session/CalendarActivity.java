package com.example.asmfinal.Session;

import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.asmfinal.database.DatabaseHelper;
import com.example.asmfinal.R;
import com.example.asmfinal.model.Transaction;
import com.example.asmfinal.adapter.TransactionAdapter;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private Calendar currentCalendar;
    private GridLayout calendarGrid;
    private TextView tvCurrentMonth;
    private TextView tvTotalIncome, tvTotalExpense, tvTotal;
    private ImageButton btnPreviousMonth, btnNextMonth;
    private RecyclerView recyclerViewTransactions;
    private TransactionAdapter transactionAdapter;

    private List<Transaction> allTransactions;
    private List<Transaction> selectedDayTransactions;
    private DecimalFormat currencyFormat;
    private SimpleDateFormat monthYearFormat;
    private SimpleDateFormat dbDateFormat;
    private DatabaseHelper databaseHelper;
    private View lastSelectedDayView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_calendar);

        initViews();
        initData();
        setupRecyclerView();
        updateCalendarDisplay();
    }

    private void initViews() {
        calendarGrid = findViewById(R.id.calendarGrid);
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvTotal = findViewById(R.id.tvTotal);
        btnPreviousMonth = findViewById(R.id.btnPreviousMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        recyclerViewTransactions = findViewById(R.id.recyclerViewTransactions);

        btnPreviousMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendarDisplay();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendarDisplay();
        });
    }

    private void initData() {
        databaseHelper = new DatabaseHelper(this);
        currentCalendar = Calendar.getInstance();
        currencyFormat = new DecimalFormat("#,###");
        monthYearFormat = new SimpleDateFormat("MMMM yyyy", new Locale("vi", "VN"));
        dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        allTransactions = new ArrayList<>();
        selectedDayTransactions = new ArrayList<>();
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(this, selectedDayTransactions);
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTransactions.setAdapter(transactionAdapter);
    }

    private void updateCalendarDisplay() {
        tvCurrentMonth.setText(monthYearFormat.format(currentCalendar.getTime()));
        calendarGrid.removeAllViews();
        allTransactions = getTransactionsForCurrentMonthFromDb();
        selectedDayTransactions.clear();
        transactionAdapter.notifyDataSetChanged();
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
        View emptyView = LayoutInflater.from(this).inflate(R.layout.calendar_day_empty, calendarGrid, false);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        emptyView.setLayoutParams(params);
        calendarGrid.addView(emptyView);
    }

    private void addDayView(int day) {
        View dayView = LayoutInflater.from(this).inflate(R.layout.calendar_day_item, calendarGrid, false);
        TextView tvDay = dayView.findViewById(R.id.tvDay);
        TextView tvAmount = dayView.findViewById(R.id.tvAmount);
        View indicatorIncome = dayView.findViewById(R.id.indicatorIncome);
        View indicatorExpense = dayView.findViewById(R.id.indicatorExpense);

        tvDay.setText(String.valueOf(day));
        List<Transaction> dayTransactions = getTransactionsForDay(day);
        double totalIncome = 0;
        double totalExpense = 0;

        for (Transaction transaction : dayTransactions) {
            if (transaction.isIncome()) {
                totalIncome += transaction.getAmount();
            } else {
                totalExpense += Math.abs(transaction.getAmount());
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
                        getResources().getColor(R.color.green_positive) :
                        getResources().getColor(R.color.red_negative));
            } else {
                tvAmount.setVisibility(View.GONE);
            }
        }

        dayView.setOnClickListener(v -> {
            showTransactionsForDay(day);
            highlightSelectedDay(dayView);
        });

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(4, 4, 4, 4);
        dayView.setLayoutParams(params);
        calendarGrid.addView(dayView);
    }

    private List<Transaction> getTransactionsForDay(int day) {
        List<Transaction> dayTransactions = new ArrayList<>();
        Calendar dayCalendar = (Calendar) currentCalendar.clone();
        dayCalendar.set(Calendar.DAY_OF_MONTH, day);

        for (Transaction transaction : allTransactions) {
            Calendar transactionCal = Calendar.getInstance();
            transactionCal.setTime(transaction.getDate());

            if (transactionCal.get(Calendar.YEAR) == dayCalendar.get(Calendar.YEAR) &&
                    transactionCal.get(Calendar.MONTH) == dayCalendar.get(Calendar.MONTH) &&
                    transactionCal.get(Calendar.DAY_OF_MONTH) == day) {
                dayTransactions.add(transaction);
            }
        }
        return dayTransactions;
    }

    private void showTransactionsForDay(int day) {
        selectedDayTransactions.clear();
        selectedDayTransactions.addAll(getTransactionsForDay(day));
        transactionAdapter.notifyDataSetChanged();
    }

    private void highlightSelectedDay(View selectedDayView) {
        if (lastSelectedDayView != null) {
            lastSelectedDayView.setBackgroundResource(R.drawable.calendar_day_background);
        }
        selectedDayView.setBackgroundResource(R.drawable.calendar_day_selected_background);
        lastSelectedDayView = selectedDayView;
    }

    private void updateMonthlySummary() {
        double totalIncome = 0;
        double totalExpense = 0;

        for (Transaction transaction : allTransactions) {
            if (transaction.isIncome()) {
                totalIncome += transaction.getAmount();
            } else {
                totalExpense += Math.abs(transaction.getAmount());
            }
        }

        double netTotal = totalIncome - totalExpense;

        tvTotalIncome.setText(formatCurrency(totalIncome) + " VND");
        tvTotalExpense.setText("-" + formatCurrency(totalExpense) + " VND");
        tvTotal.setText(formatCurrency(netTotal) + " VND");

        tvTotalIncome.setTextColor(getResources().getColor(R.color.green_positive));
        tvTotalExpense.setTextColor(getResources().getColor(R.color.red_negative));
        tvTotal.setTextColor(netTotal >= 0 ?
                getResources().getColor(R.color.green_positive) :
                getResources().getColor(R.color.red_negative));
    }

    private String formatCurrency(double amount) {
        return currencyFormat.format(Math.abs(amount));
    }

    private List<Transaction> getTransactionsForCurrentMonthFromDb() {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Calendar monthStart = (Calendar) currentCalendar.clone();
        monthStart.set(Calendar.DAY_OF_MONTH, 1);
        Calendar monthEnd = (Calendar) currentCalendar.clone();
        monthEnd.set(Calendar.DAY_OF_MONTH, monthEnd.getActualMaximum(Calendar.DAY_OF_MONTH));
        String startDateString = dbDateFormat.format(monthStart.getTime());
        String endDateString = dbDateFormat.format(monthEnd.getTime());
        String selection = DatabaseHelper.COLUMN_DATE + " BETWEEN ? AND ?";
        String[] selectionArgs = {startDateString, endDateString};

        Cursor cursor = db.query(DatabaseHelper.TABLE_EXPENSE, null, selection, selectionArgs, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EXPENSE_ID);
                int descriptionIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DESCRIPTION);
                int amountIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_AMOUNT);
                int dateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE);
                int categoryIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORY);
                do {
                    if (idIndex != -1 && descriptionIndex != -1 && amountIndex != -1 && dateIndex != -1 && categoryIndex != -1) {
                        int id = cursor.getInt(idIndex);
                        String description = cursor.getString(descriptionIndex);
                        double amount = cursor.getDouble(amountIndex);
                        String dateString = cursor.getString(dateIndex);
                        String categoryName = cursor.getString(categoryIndex);
                        Date date = null;
                        try {
                            date = dbDateFormat.parse(dateString);
                        } catch (ParseException e) {
                            Log.e("CalendarActivity", "Lỗi phân tích ngày tháng: " + dateString, e);
                        }
                        int categoryIcon = R.drawable.ic_category_default;
                        if (date != null) {
                            transactions.add(new Transaction(id, description, amount, date, categoryName, categoryIcon));
                        }
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return transactions;
    }
}