package com.example.asmfinal.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView; // Thêm import này

import com.example.asmfinal.R;
import com.example.asmfinal.database.DatabaseHelper;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChartFragment extends Fragment {

    private DatabaseHelper databaseHelper;
    private DecimalFormat formatter;

    private TextView btnWeek, btnMonth, btnYear;
    private TextView tvDateRange, tvExpenseAmount, tvIncomeAmount, tvBalance;
    private ImageButton btnPreviousPeriod, btnNextPeriod;
    // Sửa kiểu của chartCard và balanceCard từ LinearLayout sang CardView
    private CardView chartCard, balanceCard;
    private LinearLayout summaryContainer, noDataContainer;
    private FrameLayout pieChartFrame;
    private PieChart spendingChart;

    private Calendar currentPeriodStart;
    private String currentPeriod = "week";
    private SimpleDateFormat displayDateFormat;
    private SimpleDateFormat queryDateFormat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        initializeViews(view);
        initializeDatabase();
        setupClickListeners();
        formatter = new DecimalFormat("#,###");
        displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        queryDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        currentPeriodStart = Calendar.getInstance();
        updatePeriodSelectorUI();
        loadSpendingData();
        return view;
    }

    private void initializeViews(View view) {
        btnWeek = view.findViewById(R.id.btnWeek);
        btnMonth = view.findViewById(R.id.btnMonth);
        btnYear = view.findViewById(R.id.btnYear);

        // Ánh xạ các View đúng kiểu
        chartCard = view.findViewById(R.id.chartCard);
        summaryContainer = view.findViewById(R.id.summaryContainer);
        balanceCard = view.findViewById(R.id.balanceCard);
        noDataContainer = view.findViewById(R.id.noDataContainer);
        pieChartFrame = view.findViewById(R.id.pieChartFrame);

        tvDateRange = view.findViewById(R.id.tvDateRange);
        btnPreviousPeriod = view.findViewById(R.id.btnPreviousPeriod);
        btnNextPeriod = view.findViewById(R.id.btnNextPeriod);

        tvExpenseAmount = view.findViewById(R.id.tvExpenseAmount);
        tvIncomeAmount = view.findViewById(R.id.tvIncomeAmount);
        tvBalance = view.findViewById(R.id.tvBalance);

        spendingChart = new PieChart(requireContext());
        pieChartFrame.addView(spendingChart, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private void initializeDatabase() {
        databaseHelper = new DatabaseHelper(requireContext());
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
        tvDateRange.setText(displayDateFormat.format(currentPeriodStart.getTime())
                + " - " + displayDateFormat.format(calendarEnd.getTime()));

        Map<String, Float> spendingMap = databaseHelper.getSpendingByCategory(startDate, endDate);
        float totalExpense = 0;
        float totalIncome = 0;
        List<PieEntry> entries = new ArrayList<>();

        if (spendingMap != null) {
            for (Map.Entry<String, Float> entry : spendingMap.entrySet()) {
                if (entry.getValue() > 0) {
                    entries.add(new PieEntry(entry.getValue(), entry.getKey()));
                    totalExpense += entry.getValue();
                }
            }
        }

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
        data.setValueFormatter(new PercentFormatter(spendingChart));

        spendingChart.setData(data);
        spendingChart.setUsePercentValues(true);
        spendingChart.getDescription().setEnabled(false);
        spendingChart.setDrawHoleEnabled(true);
        spendingChart.setCenterText("Tổng chi\n" + formatter.format(data.getYValueSum()));
        spendingChart.animateY(1400);
        spendingChart.invalidate();
    }

    private void updateSummaryCards(float totalExpense, float totalIncome) {
        tvExpenseAmount.setText("-" + formatter.format(totalExpense) + " VND");
        tvIncomeAmount.setText(formatter.format(totalIncome) + " VND");
        float balance = totalIncome - totalExpense;
        tvBalance.setText(formatter.format(balance) + " VND");
        Context context = requireContext();
        int green = context.getResources().getColor(R.color.green_light);
        int red = context.getResources().getColor(R.color.red);
        tvBalance.setTextColor(balance >= 0 ? green : red);
    }
}