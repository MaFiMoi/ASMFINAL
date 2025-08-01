package com.example.asmfinal.Session;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.asmfinal.R;
import com.example.asmfinal.fragment.CalendarFragment;
import com.example.asmfinal.fragment.ChartFragment;
import com.example.asmfinal.fragment.HomeFragment;
import com.example.asmfinal.fragment.AccountFragment;

public class MainActivity extends AppCompatActivity {

    private Fragment homeFragment = new HomeFragment();
    private Fragment calendarFragment = new CalendarFragment();
    private Fragment chartFragment = new ChartFragment();
    private Fragment accountFragment = new AccountFragment();
    private Fragment activeFragment = homeFragment;

    private LinearLayout navHomeLayout, navCalendarLayout, navChartLayout, navAccountLayout;
    private ImageView icHome, icCalendar, icChart, icAccount;
    private TextView tvHome, tvCalendar, tvChart, tvAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupBottomNavigation();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, accountFragment).hide(accountFragment)
                .add(R.id.fragment_container, chartFragment).hide(chartFragment)
                .add(R.id.fragment_container, calendarFragment).hide(calendarFragment)
                .add(R.id.fragment_container, homeFragment)
                .commit();
    }

    private void initializeViews() {
        navHomeLayout = findViewById(R.id.nav_home_layout);
        navCalendarLayout = findViewById(R.id.nav_calendar_layout);
        navChartLayout = findViewById(R.id.nav_chart_layout);
        navAccountLayout = findViewById(R.id.nav_account_layout);

        icHome = findViewById(R.id.ic_home);
        icCalendar = findViewById(R.id.ic_calendar);
        icChart = findViewById(R.id.ic_chart);
        icAccount = findViewById(R.id.ic_account);

        tvHome = findViewById(R.id.tv_home);
        tvCalendar = findViewById(R.id.tv_calendar);
        tvChart = findViewById(R.id.tv_chart);
        tvAccount = findViewById(R.id.tv_account);
    }

    private void setupBottomNavigation() {
        navHomeLayout.setOnClickListener(v -> {
            switchFragment(homeFragment);
            setActiveTab(icHome, tvHome);
        });

        navCalendarLayout.setOnClickListener(v -> {
            switchFragment(calendarFragment);
            setActiveTab(icCalendar, tvCalendar);
        });

        navChartLayout.setOnClickListener(v -> {
            switchFragment(chartFragment);
            setActiveTab(icChart, tvChart);
        });

        navAccountLayout.setOnClickListener(v -> {
            switchFragment(accountFragment);
            setActiveTab(icAccount, tvAccount);
        });
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .hide(activeFragment)
                .show(fragment)
                .commit();
        activeFragment = fragment;
    }

    private void setActiveTab(ImageView activeIcon, TextView activeText) {
        int inactiveColor = ContextCompat.getColor(this, R.color.inactive_tab_color);
        int activeColor = ContextCompat.getColor(this, R.color.active_tab_color);

        icHome.setColorFilter(inactiveColor);
        icCalendar.setColorFilter(inactiveColor);
        icChart.setColorFilter(inactiveColor);
        icAccount.setColorFilter(inactiveColor);

        tvHome.setTextColor(inactiveColor);
        tvCalendar.setTextColor(inactiveColor);
        tvChart.setTextColor(inactiveColor);
        tvAccount.setTextColor(inactiveColor);

        activeIcon.setColorFilter(activeColor);
        activeText.setTextColor(activeColor);
    }
}