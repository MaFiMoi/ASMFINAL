package com.example.asmfinal.Session;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.FrameLayout;

import com.example.asmfinal.R;
import com.example.asmfinal.database.DatabaseHelper;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChartActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private DecimalFormat formatter;

    // View components
    private TextView btnWeek, btnMonth, btnYear;
    private TextView tvDateRange, tvExpenseAmount, tvIncomeAmount, tvBalance;
    private ImageButton btnPreviousPeriod, btnNextPeriod;
    private LinearLayout chartCard, summaryContainer, balanceCard, noDataContainer;
    private FrameLayout pieChartFrame;
    private PieChart spendingChart;

    // Navigation and state
    private LinearLayout navHome, navCalendar, navChart, navAccount;

    // State variables
    private Calendar currentPeriodStart;
    private String currentPeriod = "week"; // Default period
    private SimpleDateFormat displayDateFormat;
    private SimpleDateFormat queryDateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_chart);

        initializeViews();
        initializeDatabase();
        setupClickListeners();
        setupBottomNavigation();

        // Initialize state
        formatter = new DecimalFormat("#,###");
        displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        queryDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        currentPeriodStart = Calendar.getInstance();

        // Load initial data
        updatePeriodSelectorUI();
        loadSpendingData();
    }

    private void initializeViews() {
        // Period Selector
        btnWeek = findViewById(R.id.btnWeek);
        btnMonth = findViewById(R.id.btnMonth);
        btnYear = findViewById(R.id.btnYear);

        // Chart components
        chartCard = findViewById(R.id.chartCard);
        summaryContainer = findViewById(R.id.summaryContainer);
        balanceCard = findViewById(R.id.balanceCard);
        noDataContainer = findViewById(R.id.noDataContainer);
        pieChartFrame = findViewById(R.id.pieChartFrame);

        // Period navigation
        tvDateRange = findViewById(R.id.tvDateRange);
        btnPreviousPeriod = findViewById(R.id.btnPreviousPeriod);
        btnNextPeriod = findViewById(R.id.btnNextPeriod);

        // Summary TextViews
        tvExpenseAmount = findViewById(R.id.tvExpenseAmount);
        tvIncomeAmount = findViewById(R.id.tvIncomeAmount);
        tvBalance = findViewById(R.id.tvBalance);

        // Dynamically add PieChart to FrameLayout
        spendingChart = new PieChart(this);
        pieChartFrame.addView(spendingChart, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Bottom Navigation
        navHome = findViewById(R.id.nav_home_layout);
        navCalendar = findViewById(R.id.nav_calendar_layout);
        navChart = findViewById(R.id.nav_chart_layout);
        navAccount = findViewById(R.id.nav_account_layout);
    }

    private void initializeDatabase() {
        databaseHelper = new DatabaseHelper(this);
    }

    private void setupClickListeners() {
        btnWeek.setOnClickListener(v -> {
            currentPeriod = "week";
            updatePeriodSelectorUI();
            loadSpendingData();
        });
        btnMonth.setOnClickListener(v -> {
            currentPeriod = "month";
            updatePeriodSelectorUI();
            loadSpendingData();
        });
        btnYear.setOnClickListener(v -> {
            currentPeriod = "year";
            updatePeriodSelectorUI();
            loadSpendingData();
        });

        btnPreviousPeriod.setOnClickListener(v -> {
            movePeriod(-1);
        });
        btnNextPeriod.setOnClickListener(v -> {
            movePeriod(1);
        });
    }

    private void setupBottomNavigation() {
        // Highlight chart icon as active
        // This is a placeholder since I don't have the full bottom nav code.
        // You should implement a method like setActiveTab to set colors.
        // For now, I assume Chart is already highlighted in XML.

        navHome.setOnClickListener(v -> {
            // Start MainActivity and finish this activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        navCalendar.setOnClickListener(v -> {
            // Start CalendarActivity and finish this activity
            Intent intent = new Intent(this, CalendarActivity.class);
            startActivity(intent);
            finish();
        });

        navChart.setOnClickListener(v -> {
            // Do nothing, already here
        });

        navAccount.setOnClickListener(v -> {
            // Start AccountActivity and finish this activity
            // Intent intent = new Intent(this, AccountActivity.class);
            // startActivity(intent);
            // finish();
        });
    }

    /**
     * Cập nhật giao diện của bộ chọn thời gian (Tuần/Tháng/Năm)
     */
    private void updatePeriodSelectorUI() {
        btnWeek.setSelected(false);
        btnMonth.setSelected(false);
        btnYear.setSelected(false);

        if (currentPeriod.equals("week")) {
            btnWeek.setSelected(true);
        } else if (currentPeriod.equals("month")) {
            btnMonth.setSelected(true);
        } else {
            btnYear.setSelected(true);
        }
    }

    /**
     * Di chuyển qua lại giữa các khoảng thời gian (trước/sau)
     * @param direction -1 for previous, 1 for next
     */
    private void movePeriod(int direction) {
        if (currentPeriod.equals("week")) {
            currentPeriodStart.add(Calendar.WEEK_OF_YEAR, direction);
        } else if (currentPeriod.equals("month")) {
            currentPeriodStart.add(Calendar.MONTH, direction);
        } else {
            currentPeriodStart.add(Calendar.YEAR, direction);
        }
        loadSpendingData();
    }

    /**
     * Tải dữ liệu và cập nhật biểu đồ
     */
    private void loadSpendingData() {
        String startDate, endDate;
        Calendar calendarEnd = (Calendar) currentPeriodStart.clone();

        if (currentPeriod.equals("week")) {
            calendarEnd.add(Calendar.DAY_OF_YEAR, 6);
        } else if (currentPeriod.equals("month")) {
            calendarEnd.set(Calendar.DAY_OF_MONTH, currentPeriodStart.getActualMaximum(Calendar.DAY_OF_MONTH));
        } else {
            calendarEnd.set(Calendar.DAY_OF_YEAR, currentPeriodStart.getActualMaximum(Calendar.DAY_OF_YEAR));
        }

        startDate = queryDateFormat.format(currentPeriodStart.getTime());
        endDate = queryDateFormat.format(calendarEnd.getTime());

        // Update date range TextView
        String dateRangeText = displayDateFormat.format(currentPeriodStart.getTime()) + " - " + displayDateFormat.format(calendarEnd.getTime());
        tvDateRange.setText(dateRangeText);

        // Load data from DB (assuming getSpendingByCategory is implemented)
        Map<String, Float> spendingMap = databaseHelper.getSpendingByCategory(startDate, endDate);

        // This is a placeholder for income data, as your DB doesn't have it yet.
        // You would need to query for income separately.
        float totalIncome = 0;
        float totalExpense = 0;

        List<PieEntry> entries = new ArrayList<>();
        if (spendingMap != null && !spendingMap.isEmpty()) {
            for (Map.Entry<String, Float> entry : spendingMap.entrySet()) {
                if (entry.getValue() > 0) {
                    entries.add(new PieEntry(entry.getValue(), entry.getKey()));
                    totalExpense += entry.getValue();
                }
            }
        }

        // Update UI based on data availability
        if (entries.isEmpty()) {
            chartCard.setVisibility(View.GONE);
            summaryContainer.setVisibility(View.GONE);
            balanceCard.setVisibility(View.GONE);
            noDataContainer.setVisibility(View.VISIBLE);
        } else {
            chartCard.setVisibility(View.VISIBLE);
            summaryContainer.setVisibility(View.VISIBLE);
            balanceCard.setVisibility(View.VISIBLE);
            noDataContainer.setVisibility(View.GONE);

            updatePieChart(entries);
            updateSummaryCards(totalExpense, totalIncome);
        }
    }

    /**
     * Cập nhật biểu đồ tròn với dữ liệu mới
     */
    private void updatePieChart(List<PieEntry> entries) {
        PieDataSet dataSet = new PieDataSet(entries, "Chi tiêu");

        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS) colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS) colors.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS) colors.add(c);
        for (int c : ColorTemplate.LIBERTY_COLORS) colors.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS) colors.add(c);
        dataSet.setColors(colors);

        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(11f);
        data.setValueTextColor(android.R.color.white);
        data.setValueFormatter(new PercentFormatter(spendingChart));

        spendingChart.setData(data);
        spendingChart.setUsePercentValues(true);
        spendingChart.getDescription().setEnabled(false);
        spendingChart.setEntryLabelColor(android.R.color.white);
        spendingChart.setCenterText("Tổng chi\n" + formatter.format(data.getYValueSum()));
        spendingChart.setCenterTextColor(android.R.color.white);
        spendingChart.setCenterTextSize(14f);
        spendingChart.setDrawHoleEnabled(true);
        spendingChart.setHoleColor(android.R.color.transparent);

        Legend legend = spendingChart.getLegend();
        legend.setEnabled(false);

        spendingChart.animateY(1400);
        spendingChart.invalidate();
    }
    /**
     * Cập nhật các thẻ tóm tắt chi tiêu
     */
    private void updateSummaryCards(float totalExpense, float totalIncome) {
        tvExpenseAmount.setText("-" + formatter.format(totalExpense) + " VND");
        tvIncomeAmount.setText(formatter.format(totalIncome) + " VND"); // Placeholder for income
        float balance = totalIncome - totalExpense;
        tvBalance.setText(formatter.format(balance) + " VND");
        if (balance >= 0) {
            tvBalance.setTextColor(getResources().getColor(R.color.green_light)); // Assuming you have a green color
        } else {
            tvBalance.setTextColor(getResources().getColor(R.color.red)); // Assuming you have a red color
        }
    }
}