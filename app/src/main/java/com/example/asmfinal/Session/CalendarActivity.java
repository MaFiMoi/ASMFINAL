package com.example.asmfinal.Session;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.asmfinal.database.DatabaseHelper;
import com.example.asmfinal.R;
import com.example.asmfinal.model.Transaction;
import com.example.asmfinal.model.TransactionType;
import com.example.asmfinal.adapter.TransactionAdapter;

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
        setContentView(R.layout.calendar_activity);

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
        // This ID now exists in the corrected XML
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
        // Using US locale for English format
        monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);

        allTransactions = new ArrayList<>();
        selectedDayTransactions = new ArrayList<>();
    }

    private void setupCalendar() {
        // Calendar is set up dynamically in updateCalendarDisplay()
    }

    private void setupRecyclerView() {
        // The corrected XML uses recyclerViewTransactions, so this is correct.
        transactionAdapter = new TransactionAdapter(this, selectedDayTransactions);
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

        // Adjust for Monday start (Monday=1, Sunday=7 in ISO 8601)
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
            } else {
                indicatorIncome.setVisibility(View.GONE);
            }
            if (totalExpense > 0) {
                indicatorExpense.setVisibility(View.VISIBLE);
            } else {
                indicatorExpense.setVisibility(View.GONE);
            }

            // Show net amount
            double netAmount = totalIncome - totalExpense;
            // You can adjust this threshold
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
        // You might want to scroll to the top of the RecyclerView here
        // recyclerViewTransactions.scrollToPosition(0);
    }

    private void highlightSelectedDay(View selectedDayView) {
        // Remove highlight from all days
        for (int i = 0; i < calendarGrid.getChildCount(); i++) {
            View child = calendarGrid.getChildAt(i);
            child.setBackgroundResource(R.drawable.calendar_day_background); // Assuming a default background
        }
        // Highlight selected day
        selectedDayView.setBackgroundResource(R.drawable.calendar_day_selected_background); // Assuming a selected background
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

        // Set colors (Ensure these color resources exist in colors.xml)
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
        monthStart.set(Calendar.MILLISECOND, 0);

        Calendar monthEnd = (Calendar) monthStart.clone();
        monthEnd.add(Calendar.MONTH, 1);
        monthEnd.add(Calendar.MILLISECOND, -1);

        String selection = DatabaseHelper.COLUMN_EXPENSE_DATE + " BETWEEN ? AND ?";
        String[] selectionArgs = {
                String.valueOf(monthStart.getTimeInMillis()),
                String.valueOf(monthEnd.getTimeInMillis())
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

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                // Get column indices once for efficiency
                int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EXPENSE_ID);
                int descriptionIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EXPENSE_NOTE);
                int amountIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EXPENSE_AMOUNT);
                int timestampIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EXPENSE_DATE);
                int categoryIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EXPENSE_CATEGORY_ID);

                do {
                    // Check if indices are valid before getting data
                    if (idIndex != -1 && descriptionIndex != -1 && amountIndex != -1 && timestampIndex != -1 && categoryIdIndex != -1) {
                        // int id = cursor.getInt(idIndex); // Not used
                        String description = cursor.getString(descriptionIndex);
                        double amount = cursor.getDouble(amountIndex);
                        long timestamp = cursor.getLong(timestampIndex);
                        // int categoryId = cursor.getInt(categoryIdIndex); // Not used

                        // You'll need logic to fetch the category name and icon based on categoryId
                        // For now, we'll use a placeholder
                        // String categoryName = "Default"; // Not used
                        int categoryIcon = R.drawable.ic_category_default; // Make sure this drawable exists

                        TransactionType transactionType = (amount > 0) ? TransactionType.INCOME : TransactionType.EXPENSE;

                        transactions.add(new Transaction(description, amount, new Date(timestamp), transactionType, categoryIcon));
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();

        return transactions;
    }
}