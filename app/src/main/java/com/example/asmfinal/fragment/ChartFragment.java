package com.example.asmfinal.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.asmfinal.R;
import com.example.asmfinal.adapter.Expense;
import com.example.asmfinal.database.DatabaseHelper;
import com.example.asmfinal.model.SharedViewModel;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChartFragment extends Fragment {

    private static final String TAG = "ChartFragment";
    private DatabaseHelper databaseHelper;
    private SharedViewModel sharedViewModel;
    private DecimalFormat formatter;

    private TextView btnWeek, btnMonth, btnYear;
    private TextView tvDateRange, tvExpenseAmount, tvIncomeAmount, tvBalance;
    private ImageButton btnPreviousPeriod, btnNextPeriod;
    private CardView chartCard, balanceCard, barChartCard;
    private LinearLayout summaryContainer, noDataContainer;
    private FrameLayout pieChartFrame, barChartFrame;
    private PieChart spendingChart;
    private BarChart barChart;
    private TextView btnPieChart, btnBarChart;

    private Calendar currentPeriodStart;
    private String currentPeriod = "week";
    private String currentChartType = "pie"; // pie hoặc bar
    private SimpleDateFormat displayDateFormat;
    private SimpleDateFormat queryDateFormat;
    private Map<String, String> categoryNames = new HashMap<>();

    // Danh sách expenses hiện tại
    private List<Expense> currentExpenses = new ArrayList<>();

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
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        initializeViews(view);
        initializeDatabase();
        initializeCategories();
        formatter = new DecimalFormat("#,###");
        displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        queryDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        currentPeriodStart = Calendar.getInstance();
        setupClickListeners();
        updatePeriodSelectorUI();
        updateChartTypeUI();

        // Quan sát dữ liệu từ SharedViewModel
        observeExpenseData();

        // Load dữ liệu ban đầu
        loadSpendingData();
        return view;
    }

    private void observeExpenseData() {
        sharedViewModel.getExpenses().observe(getViewLifecycleOwner(), expenses -> {
            Log.d(TAG, "Expense data changed, updating chart");
            currentExpenses = new ArrayList<>(expenses);
            loadSpendingData();
        });
    }

    private void initializeViews(View view) {
        btnWeek = view.findViewById(R.id.btnWeek);
        btnMonth = view.findViewById(R.id.btnMonth);
        btnYear = view.findViewById(R.id.btnYear);
        btnPieChart = view.findViewById(R.id.btnPieChart);
        btnBarChart = view.findViewById(R.id.btnBarChart);
        chartCard = view.findViewById(R.id.chartCard);
        barChartCard = view.findViewById(R.id.barChartCard);
        summaryContainer = view.findViewById(R.id.summaryContainer);
        balanceCard = view.findViewById(R.id.balanceCard);
        noDataContainer = view.findViewById(R.id.noDataContainer);
        pieChartFrame = view.findViewById(R.id.pieChartFrame);
        barChartFrame = view.findViewById(R.id.barChartFrame);
        tvDateRange = view.findViewById(R.id.tvDateRange);
        btnPreviousPeriod = view.findViewById(R.id.btnPreviousPeriod);
        btnNextPeriod = view.findViewById(R.id.btnNextPeriod);
        tvExpenseAmount = view.findViewById(R.id.tvExpenseAmount);
        tvIncomeAmount = view.findViewById(R.id.tvIncomeAmount);
        tvBalance = view.findViewById(R.id.tvBalance);

        // Khởi tạo PieChart
        spendingChart = new PieChart(requireContext());
        pieChartFrame.addView(spendingChart, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // Khởi tạo BarChart
        barChart = new BarChart(requireContext());
        barChartFrame.addView(barChart, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private void initializeDatabase() {
        databaseHelper = new DatabaseHelper(requireContext());
    }

    private void initializeCategories() {
        categoryNames.put("food", "Ăn uống");
        categoryNames.put("transport", "Di chuyển");
        categoryNames.put("water", "Tiền nước");
        categoryNames.put("phone", "Tiền điện thoại");
        categoryNames.put("electricity", "Tiền điện");
        categoryNames.put("maintenance", "Bảo dưỡng xe");
        categoryNames.put("shopping", "Mua sắm");
        categoryNames.put("entertainment", "Giải trí");
        categoryNames.put("health", "Sức khỏe");
        categoryNames.put("education", "Giáo dục");
        categoryNames.put("other", "Khác");
    }

    private void setupClickListeners() {
        btnWeek.setOnClickListener(v -> {
            currentPeriod = "week";
            currentPeriodStart = Calendar.getInstance();
            currentPeriodStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            updatePeriodSelectorUI();
            loadSpendingData();
        });

        btnMonth.setOnClickListener(v -> {
            currentPeriod = "month";
            currentPeriodStart = Calendar.getInstance();
            currentPeriodStart.set(Calendar.DAY_OF_MONTH, 1);
            updatePeriodSelectorUI();
            loadSpendingData();
        });

        btnYear.setOnClickListener(v -> {
            currentPeriod = "year";
            currentPeriodStart = Calendar.getInstance();
            currentPeriodStart.set(Calendar.DAY_OF_YEAR, 1);
            updatePeriodSelectorUI();
            loadSpendingData();
        });

        btnPieChart.setOnClickListener(v -> {
            currentChartType = "pie";
            updateChartTypeUI();
            loadSpendingData();
        });

        btnBarChart.setOnClickListener(v -> {
            currentChartType = "bar";
            updateChartTypeUI();
            loadSpendingData();
        });

        btnPreviousPeriod.setOnClickListener(v -> movePeriod(-1));
        btnNextPeriod.setOnClickListener(v -> movePeriod(1));
    }

    private void updatePeriodSelectorUI() {
        btnWeek.setSelected(false);
        btnMonth.setSelected(false);
        btnYear.setSelected(false);

        switch (currentPeriod) {
            case "week":
                btnWeek.setSelected(true);
                break;
            case "month":
                btnMonth.setSelected(true);
                break;
            case "year":
                btnYear.setSelected(true);
                break;
        }
    }

    private void updateChartTypeUI() {
        btnPieChart.setSelected(currentChartType.equals("pie"));
        btnBarChart.setSelected(currentChartType.equals("bar"));

        if (currentChartType.equals("pie")) {
            chartCard.setVisibility(View.VISIBLE);
            barChartCard.setVisibility(View.GONE);
        } else {
            chartCard.setVisibility(View.GONE);
            barChartCard.setVisibility(View.VISIBLE);
        }
    }

    private void movePeriod(int direction) {
        switch (currentPeriod) {
            case "week":
                currentPeriodStart.add(Calendar.WEEK_OF_YEAR, direction);
                break;
            case "month":
                currentPeriodStart.add(Calendar.MONTH, direction);
                break;
            case "year":
                currentPeriodStart.add(Calendar.YEAR, direction);
                break;
        }
        loadSpendingData();
    }

    private void loadSpendingData() {
        Calendar calendarEnd = (Calendar) currentPeriodStart.clone();
        switch (currentPeriod) {
            case "week":
                calendarEnd.add(Calendar.DAY_OF_YEAR, 6);
                break;
            case "month":
                calendarEnd.set(Calendar.DAY_OF_MONTH,
                        currentPeriodStart.getActualMaximum(Calendar.DAY_OF_MONTH));
                break;
            case "year":
                calendarEnd.set(Calendar.DAY_OF_YEAR,
                        currentPeriodStart.getActualMaximum(Calendar.DAY_OF_YEAR));
                break;
        }

        String startDate = queryDateFormat.format(currentPeriodStart.getTime());
        String endDate = queryDateFormat.format(calendarEnd.getTime());

        Log.d(TAG, "Loading data from " + startDate + " to " + endDate);

        tvDateRange.setText(displayDateFormat.format(currentPeriodStart.getTime())
                + " - " + displayDateFormat.format(calendarEnd.getTime()));

        // Lọc dữ liệu theo khoảng thời gian từ SharedViewModel
        List<Expense> filteredExpenses = filterExpensesByDateRange(currentExpenses, startDate, endDate);

        // Tính toán dữ liệu chi tiêu và thu nhập
        Map<String, Float> spendingMap = getSpendingByCategory(filteredExpenses);
        float totalIncome = getTotalIncome(filteredExpenses);
        float totalExpense = 0;

        List<PieEntry> pieEntries = new ArrayList<>();
        List<BarEntry> barEntries = new ArrayList<>();
        List<String> categoryLabels = new ArrayList<>();
        int index = 0;

        if (spendingMap != null && !spendingMap.isEmpty()) {
            for (Map.Entry<String, Float> entry : spendingMap.entrySet()) {
                if (entry.getValue() > 0) {
                    String categoryName = categoryNames.getOrDefault(entry.getKey(), "Không xác định");
                    pieEntries.add(new PieEntry(entry.getValue(), categoryName));
                    barEntries.add(new BarEntry(index, entry.getValue()));
                    categoryLabels.add(categoryName);
                    totalExpense += entry.getValue();
                    index++;
                }
            }
        }

        Log.d(TAG, "Total expense: " + totalExpense + ", Total income: " + totalIncome);
        Log.d(TAG, "Categories found: " + pieEntries.size());

        if (pieEntries.isEmpty()) {
            showNoDataState();
        } else {
            showDataState();
            if (currentChartType.equals("pie")) {
                updatePieChart(pieEntries, totalExpense);
            } else {
                updateBarChart(barEntries, categoryLabels, totalExpense);
            }
            updateSummaryCards(totalExpense, totalIncome);
        }
    }

    /**
     * Lọc expenses theo khoảng thời gian
     */
    private List<Expense> filterExpensesByDateRange(List<Expense> expenses, String startDate, String endDate) {
        List<Expense> filteredExpenses = new ArrayList<>();

        for (Expense expense : expenses) {
            String expenseDate = expense.getDate();
            if (expenseDate.compareTo(startDate) >= 0 && expenseDate.compareTo(endDate) <= 0) {
                filteredExpenses.add(expense);
            }
        }

        Log.d(TAG, "Filtered " + filteredExpenses.size() + " expenses from " + expenses.size() + " total");
        return filteredExpenses;
    }

    private void showNoDataState() {
        chartCard.setVisibility(View.GONE);
        barChartCard.setVisibility(View.GONE);
        summaryContainer.setVisibility(View.GONE);
        balanceCard.setVisibility(View.GONE);
        noDataContainer.setVisibility(View.VISIBLE);
    }

    private void showDataState() {
        summaryContainer.setVisibility(View.VISIBLE);
        balanceCard.setVisibility(View.VISIBLE);
        noDataContainer.setVisibility(View.GONE);
        updateChartTypeUI();
    }

    /**
     * Lấy dữ liệu chi tiêu theo danh mục từ danh sách expenses đã lọc
     */
    private Map<String, Float> getSpendingByCategory(List<Expense> expenses) {
        Map<String, Float> spendingMap = new HashMap<>();

        // Khởi tạo tất cả categories với giá trị 0
        for (String category : categoryNames.keySet()) {
            spendingMap.put(category, 0f);
        }

        // Tính tổng chi tiêu theo category (chỉ tính số âm - chi tiêu)
        for (Expense expense : expenses) {
            if (expense.getAmount() < 0) { // Chi tiêu là số âm
                String category = expense.getCategory();
                float currentAmount = spendingMap.getOrDefault(category, 0f);
                spendingMap.put(category, currentAmount + Math.abs(expense.getAmount()));
            }
        }

        // Log để debug
        for (Map.Entry<String, Float> entry : spendingMap.entrySet()) {
            if (entry.getValue() > 0) {
                Log.d(TAG, "Category: " + entry.getKey() + ", Amount: " + entry.getValue());
            }
        }

        return spendingMap;
    }

    /**
     * Lấy tổng thu nhập từ danh sách expenses đã lọc
     */
    private float getTotalIncome(List<Expense> expenses) {
        float totalIncome = 0f;

        // Tính tổng thu nhập (số dương)
        for (Expense expense : expenses) {
            if (expense.getAmount() > 0) { // Thu nhập là số dương
                totalIncome += expense.getAmount();
            }
        }

        Log.d(TAG, "Total income calculated: " + totalIncome);
        return totalIncome;
    }

    private void updatePieChart(List<PieEntry> entries, float totalExpense) {
        PieDataSet dataSet = new PieDataSet(entries, "Chi tiêu");
        ArrayList<Integer> colors = new ArrayList<>();

        // Thêm màu sắc đa dạng
        colors.add(Color.rgb(255, 102, 102)); // Đỏ nhạt
        colors.add(Color.rgb(102, 178, 255)); // Xanh dương nhạt
        colors.add(Color.rgb(255, 178, 102)); // Cam nhạt
        colors.add(Color.rgb(102, 255, 178)); // Xanh lá nhạt
        colors.add(Color.rgb(178, 102, 255)); // Tím nhạt
        colors.add(Color.rgb(255, 255, 102)); // Vàng nhạt
        colors.add(Color.rgb(255, 178, 178)); // Hồng nhạt
        colors.add(Color.rgb(178, 255, 255)); // Xanh ngọc nhạt
        colors.add(Color.rgb(255, 102, 255)); // Hồng tím
        colors.add(Color.rgb(178, 178, 178)); // Xám nhạt
        colors.add(Color.rgb(255, 215, 0));   // Vàng gold

        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(8f);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(spendingChart));

        spendingChart.setData(data);
        spendingChart.setUsePercentValues(true);
        spendingChart.getDescription().setEnabled(false);
        spendingChart.setDrawHoleEnabled(true);
        spendingChart.setHoleRadius(40f);
        spendingChart.setTransparentCircleRadius(45f);
        spendingChart.setCenterText("Tổng chi\n" + formatter.format(totalExpense) + " VNĐ");
        spendingChart.setCenterTextSize(14f);
        spendingChart.setCenterTextColor(Color.BLACK);
        spendingChart.setEntryLabelTextSize(10f);
        spendingChart.setEntryLabelColor(Color.BLACK);
        spendingChart.animateY(1000);
        spendingChart.invalidate();
    }

    private void updateBarChart(List<BarEntry> entries, List<String> labels, float totalExpense) {
        BarDataSet dataSet = new BarDataSet(entries, "Chi tiêu theo danh mục");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        // Formatter cho giá trị trên cột
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return formatter.format(value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);
        barChart.animateY(1000);

        // Thiết lập trục X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) {
                    return labels.get(index);
                }
                return "";
            }
        });
        xAxis.setLabelRotationAngle(-45f);
        xAxis.setTextSize(10f);

        // Thiết lập trục Y
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return formatter.format(value);
            }
        });
        leftAxis.setTextSize(10f);

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);

        barChart.invalidate();
    }

    private void updateSummaryCards(float totalExpense, float totalIncome) {
        tvExpenseAmount.setText("-" + formatter.format(totalExpense) + " VNĐ");
        tvIncomeAmount.setText("+" + formatter.format(totalIncome) + " VNĐ");
        float balance = totalIncome - totalExpense;

        int green = Color.parseColor("#4CAF50");
        int red = Color.parseColor("#F44336");
        tvBalance.setTextColor(balance >= 0 ? green : red);

        // Thêm dấu + hoặc - cho balance
        String balanceText = (balance >= 0 ? "+" : "-") + formatter.format(Math.abs(balance)) + " VNĐ";
        tvBalance.setText(balanceText);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload dữ liệu khi fragment được hiển thị lại
        loadSpendingData();
    }
}