package com.example.asmfinal.Session;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

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
    private TextView tvNoTransactions;
    private ImageButton btnPreviousMonth, btnNextMonth;
    private RecyclerView recyclerViewTransactions;
    private TransactionAdapter transactionAdapter;

    private List<Transaction> allTransactions;
    private List<Transaction> selectedDayTransactions;
    private DecimalFormat currencyFormat;
    private SimpleDateFormat monthYearFormat;

    private DatabaseHelper databaseHelper;
    private View lastSelectedDayView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        initViews();
        initData();
        setupCalendar();
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
        tvNoTransactions = findViewById(R.id.tvNoTransactions);

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
        allTransactions = new ArrayList<>();
        selectedDayTransactions = new ArrayList<>();
    }

    private void setupCalendar() {
        // Not needed
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(selectedDayTransactions);
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTransactions.setAdapter(transactionAdapter);
    }

    private void updateCalendarDisplay() {
        tvCurrentMonth.setText(monthYearFormat.format(currentCalendar.getTime()));
        calendarGrid.removeAllViews();

        allTransactions = getTransactionsForCurrentMonthFromDb();

        selectedDayTransactions.clear();
        transactionAdapter.notifyDataSetChanged();
        tvNoTransactions.setVisibility(View.VISIBLE);

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
        View emptyView = getLayoutInflater().inflate(R.layout.calendar_day_empty, calendarGrid, false);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        emptyView.setLayoutParams(params);
        calendarGrid.addView(emptyView);
    }

    private void addDayView(int day) {
        View dayView = getLayoutInflater().inflate(R.layout.calendar_day_item, calendarGrid, false);

        TextView tvDay = dayView.findViewById(R.id.tvDay);
        TextView tvAmount = dayView.findViewById(R.id.tvAmount);
        View indicatorIncome = dayView.findViewById(R.id.indicatorIncome);
        View indicatorExpense = dayView.findViewById(R.id.indicatorExpense);

        tvDay.setText(String.valueOf(day));

        List<Transaction> dayTransactions = getTransactionsForDay(day);
        double totalIncome = 0;
        double totalExpense = 0;

        for (Transaction transaction : dayTransactions) {
            if (transaction.getAmount() >= 0) {
                totalIncome += transaction.getAmount();
            } else {
                totalExpense += Math.abs(transaction.getAmount());
            }
        }

        if (totalIncome > 0 || totalExpense > 0) {
            if (totalIncome > 0) {
                indicatorIncome.setVisibility(View.VISIBLE);
            }
            if (totalExpense > 0) {
                indicatorExpense.setVisibility(View.VISIBLE);
            }
            double netAmount = totalIncome - totalExpense;
            tvAmount.setText(formatCurrency(netAmount));
            tvAmount.setVisibility(View.VISIBLE);
            tvAmount.setTextColor(netAmount >= 0 ?
                    ContextCompat.getColor(this, R.color.green_positive) :
                    ContextCompat.getColor(this, R.color.red_negative));
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
        List<Transaction> transactionsForDay = getTransactionsForDay(day);
        if (transactionsForDay.isEmpty()) {
            tvNoTransactions.setVisibility(View.VISIBLE);
            recyclerViewTransactions.setVisibility(View.GONE);
        } else {
            tvNoTransactions.setVisibility(View.GONE);
            recyclerViewTransactions.setVisibility(View.VISIBLE);
            selectedDayTransactions.clear();
            selectedDayTransactions.addAll(transactionsForDay);
            transactionAdapter.notifyDataSetChanged();
        }
    }

    private void highlightSelectedDay(View selectedDayView) {
        if (lastSelectedDayView != null) {
            lastSelectedDayView.setBackgroundResource(android.R.color.transparent);
        }
        selectedDayView.setBackgroundResource(R.drawable.calendar_day_selected_background);
        lastSelectedDayView = selectedDayView;
    }

    private void updateMonthlySummary() {
        double totalIncome = 0;
        double totalExpense = 0;

        for (Transaction transaction : allTransactions) {
            if (transaction.getAmount() >= 0) {
                totalIncome += transaction.getAmount();
            } else {
                totalExpense += Math.abs(transaction.getAmount());
            }
        }

        double netTotal = totalIncome - totalExpense;

        tvTotalIncome.setText(formatCurrency(totalIncome) + " VND");
        tvTotalExpense.setText("-" + formatCurrency(totalExpense) + " VND");
        tvTotal.setText(formatCurrency(netTotal) + " VND");

        tvTotalIncome.setTextColor(ContextCompat.getColor(this, R.color.green_positive));
        tvTotalExpense.setTextColor(ContextCompat.getColor(this, R.color.red_negative));
        tvTotal.setTextColor(netTotal >= 0 ?
                ContextCompat.getColor(this, R.color.green_positive) :
                ContextCompat.getColor(this, R.color.red_negative));
    }

    private String formatCurrency(double amount) {
        return currencyFormat.format(Math.abs(amount));
    }

    private List<Transaction> getTransactionsForCurrentMonthFromDb() {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Calendar monthStart = (Calendar) currentCalendar.clone();
        monthStart.set(Calendar.DAY_OF_MONTH, 1);
        monthStart.set(Calendar.HOUR_OF_DAY, 0);
        monthStart.set(Calendar.MINUTE, 0);
        monthStart.set(Calendar.SECOND, 0);

        Calendar monthEnd = (Calendar) monthStart.clone();
        monthEnd.add(Calendar.MONTH, 1);
        monthEnd.add(Calendar.SECOND, -1);

        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String monthStartString = dbDateFormat.format(monthStart.getTime());
        String monthEndString = dbDateFormat.format(monthEnd.getTime());

        // Sử dụng tên bảng và cột từ DatabaseHelper gốc của bạn
        String query = "SELECT " +
                "T." + DatabaseHelper.COLUMN_EXPENSE_ID + ", " +
                "T." + DatabaseHelper.COLUMN_TITLE + ", " +
                "T." + DatabaseHelper.COLUMN_AMOUNT + ", " +
                "T." + DatabaseHelper.COLUMN_DATE + ", " +
                "T." + DatabaseHelper.COLUMN_CATEGORY +
                " FROM " + DatabaseHelper.TABLE_EXPENSE + " T" +
                " WHERE " + "T." + DatabaseHelper.COLUMN_DATE + " BETWEEN ? AND ?";

        Cursor cursor = db.rawQuery(query, new String[]{monthStartString, monthEndString});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSE_ID));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT));
                String dateString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE));
                String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY));
                // Bạn có thể cần thêm logic để lấy icon dựa trên categoryName
                int categoryIcon = R.drawable.ic_category_default; // Giả sử một icon mặc định

                Date transactionDate = null;
                try {
                    transactionDate = dbDateFormat.parse(dateString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (transactionDate != null) {
                    transactions.add(new Transaction(id, description, amount, transactionDate, categoryName, categoryIcon));
                }

            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return transactions;
    }
}