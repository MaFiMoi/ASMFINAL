package com.example.asmfinal.Session;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.asmfinal.DatabaseHelper;
import com.example.asmfinal.R;
import com.example.asmfinal.model.TransactionType;

import java.text.DecimalFormat;
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

    // DatabaseHelper instance
    private DatabaseHelper databaseHelper;

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
        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        currentCalendar = Calendar.getInstance();
        currencyFormat = new DecimalFormat("#,###");
        monthYearFormat = new SimpleDateFormat("'Month' MM 'Year' yyyy", Locale.US); // Using US locale for English format

        allTransactions = new ArrayList<>();
        selectedDayTransactions = new ArrayList<>();
    }

    private void setupCalendar() {
        // Calendar is set up dynamically in updateCalendarDisplay()
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(selectedDayTransactions);
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTransactions.setAdapter(transactionAdapter);
    }

    private void updateCalendarDisplay() {
        // Update month/year header
        tvCurrentMonth.setText(monthYearFormat.format(currentCalendar.getTime()));

        // Clear existing calendar
        calendarGrid.removeAllViews();

        // Load transactions from database for the current month
        allTransactions = getTransactionsForCurrentMonthFromDb();

        // Reset selected day transactions
        selectedDayTransactions.clear();
        transactionAdapter.notifyDataSetChanged();

        // Calculate calendar data
        Calendar tempCal = (Calendar) currentCalendar.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Adjust for Monday start
        int startDay = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : firstDayOfWeek - 2;

        // Add empty cells for days before month starts
        for (int i = 0; i < startDay; i++) {
            addEmptyDayView();
        }

        // Add days of the month
        for (int day = 1; day <= daysInMonth; day++) {
            addDayView(day);
        }

        // Update monthly summary
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

        // Get transactions for this day
        List<Transaction> dayTransactions = getTransactionsForDay(day);
        double totalIncome = 0;
        double totalExpense = 0;

        for (Transaction transaction : dayTransactions) {
            if (transaction.getAmount() > 0) {
                totalIncome += transaction.getAmount();
            } else {
                totalExpense += Math.abs(transaction.getAmount());
            }
        }

        // Show indicators and amounts
        if (totalIncome > 0 || totalExpense > 0) {
            if (totalIncome > 0) {
                indicatorIncome.setVisibility(View.VISIBLE);
            }
            if (totalExpense > 0) {
                indicatorExpense.setVisibility(View.VISIBLE);
            }

            // Show net amount
            double netAmount = totalIncome - totalExpense;
            if (Math.abs(netAmount) >= 1000) { // Only show if significant amount
                tvAmount.setText(formatCurrency(netAmount));
                tvAmount.setVisibility(View.VISIBLE);
                tvAmount.setTextColor(netAmount >= 0 ?
                        getResources().getColor(R.color.green_positive) :
                        getResources().getColor(R.color.red_negative));
            }
        }

        // Handle day click
        dayView.setOnClickListener(v -> {
            showTransactionsForDay(day);
            highlightSelectedDay(dayView);
        });

        // Set layout params
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
        // Remove highlight from all days
        for (int i = 0; i < calendarGrid.getChildCount(); i++) {
            View child = calendarGrid.getChildAt(i);
            child.setSelected(false);
        }
        // Highlight selected day
        selectedDayView.setSelected(true);
    }

    private void updateMonthlySummary() {
        double totalIncome = 0;
        double totalExpense = 0;

        for (Transaction transaction : allTransactions) {
            if (transaction.getAmount() > 0) {
                totalIncome += transaction.getAmount();
            } else {
                totalExpense += Math.abs(transaction.getAmount());
            }
        }

        double netTotal = totalIncome - totalExpense;

        tvTotalIncome.setText(formatCurrency(totalIncome) + " VND");
        tvTotalExpense.setText("-" + formatCurrency(totalExpense) + " VND");
        tvTotal.setText(formatCurrency(netTotal) + " VND");

        // Set colors
        tvTotalIncome.setTextColor(getResources().getColor(R.color.green_positive));
        tvTotalExpense.setTextColor(getResources().getColor(R.color.red_negative));
        tvTotal.setTextColor(netTotal >= 0 ?
                getResources().getColor(R.color.green_positive) :
                getResources().getColor(R.color.red_negative));
    }

    private String formatCurrency(double amount) {
        return currencyFormat.format(Math.abs(amount));
    }

    /**
     * Method to fetch all transactions for the current month from the database.
     */
    private List<Transaction> getTransactionsForCurrentMonthFromDb() {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Get the start and end of the current month
        Calendar monthStart = (Calendar) currentCalendar.clone();
        monthStart.set(Calendar.DAY_OF_MONTH, 1);
        monthStart.set(Calendar.HOUR_OF_DAY, 0);
        monthStart.set(Calendar.MINUTE, 0);
        monthStart.set(Calendar.SECOND, 0);

        Calendar monthEnd = (Calendar) monthStart.clone();
        monthEnd.add(Calendar.MONTH, 1);
        monthEnd.add(Calendar.SECOND, -1);

        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String selection = DatabaseHelper.COLUMN_TRANSACTION_DATE + " BETWEEN ? AND ?";
        String[] selectionArgs = {
                dbDateFormat.format(monthStart.getTime()),
                dbDateFormat.format(monthEnd.getTime())
        };

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_EXPENSES,
                null, // Get all columns
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Assuming your column names and types match
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSE_ID));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSE_NOTE));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSE_AMOUNT));
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSE_DATE));
                int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSE_CATEGORY_ID));

                // You'll need logic to fetch the category name and icon based on categoryId
                // For now, we'll use a placeholder
                String categoryName = "Default";
                int categoryIcon = R.drawable.ic_category_default; // You need to have this drawable

                TransactionType transactionType = (amount > 0) ? TransactionType.INCOME : TransactionType.EXPENSE;

                // Create a Transaction object and add it to the list
                transactions.add(new Transaction(description, amount, new Date(timestamp), transactionType, categoryIcon));

            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        return transactions;
    }
}