package com.example.finalproject.activities;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.finalproject.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.example.finalproject.model.Habit;
import com.example.finalproject.model.HabitDatabaseHelper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ProgressWeekFragment extends Fragment {
    private HabitDatabaseHelper databaseHelper;
    private BarChart barChart;
    private List<String> x_values = Arrays.asList("", "", "", "", "", "", "");
    private List<String> full_dates = Arrays.asList("", "", "", "", "", "", "");

    public ProgressWeekFragment() {
    }

    public static ProgressWeekFragment newInstance(String idHabit, String idTaiKhoan, String test) {
        ProgressWeekFragment fragment = new ProgressWeekFragment();
        Bundle args = new Bundle();
        args.putString("idHabit", idHabit);
        args.putString("idTaiKhoan", idTaiKhoan);
        args.putString("test", test);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress_week, container, false);
        String idHabit = getArguments().getString("idHabit");
        String idTaiKhoan = getArguments().getString("idTaiKhoan");

        barChart = view.findViewById(R.id.barChart);
        setUpBarChart();
        loadXValues();
        loadBarChartData(idHabit, idTaiKhoan);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        String idHabit = getArguments().getString("idHabit");
        String idTaiKhoan = getArguments().getString("idTaiKhoan");
        if (idHabit != null && idTaiKhoan != null) {
            loadXValues();
            loadBarChartData(idHabit, idTaiKhoan);
        }
    }

    private void setUpBarChart() {
        barChart.getAxisRight().setDrawLabels(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(true);
        barChart.setBorderColor(getResources().getColor(R.color.Blue));
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
    }

    private void loadBarChartData(String idHabit, String idTaiKhoan) {
        databaseHelper = HabitDatabaseHelper.getInstance(requireContext());
        Habit habit = databaseHelper.getHabit(idTaiKhoan, idHabit);
        if (habit == null) return;

        try {
            // Tính toán mục tiêu cho mỗi thanh bar (Mục tiêu tổng / số ngày của chu kỳ)
            double totalTarget = habit.getMucTieu();
            int periodDays = getPeriodDays(habit.getKhoangThoiGian());
            double dailyShare = (periodDays > 0) ? (totalTarget / periodDays) : totalTarget;

            ArrayList<BarEntry> barEntriesArrayList = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                String dateStr = full_dates.get(i);
                double dailyValue = databaseHelper.getProgressByDate(idTaiKhoan, idHabit, dateStr);
                
                // Hiển thị % hoàn thành của ngày đó so với "phần" mục tiêu của ngày đó
                float percent = (dailyShare > 0) ? (float)(dailyValue * 100.0 / dailyShare) : 0f;
                barEntriesArrayList.add(new BarEntry(i, Math.min(100f, percent)));
            }

            YAxis yAxis = barChart.getAxisLeft();
            yAxis.setAxisMinimum(0);
            yAxis.setAxisMaximum(100);
            yAxis.setLabelCount(6);

            BarDataSet barDataSet = new BarDataSet(barEntriesArrayList, "Tiến độ ngày (%)");
            barDataSet.setColor(getResources().getColor(R.color.Blue));
            barDataSet.setValueTextSize(10f);

            BarData barData = new BarData(barDataSet);
            barChart.setData(barData);
            
            XAxis xAxis = barChart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(x_values));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            
            barChart.invalidate();
        } catch (Exception e) {
            Log.e("ProgressWeek", "Error loading chart: " + e.getMessage());
        }
    }

    private int getPeriodDays(String period) {
        if (period == null) return 1;
        if (period.equalsIgnoreCase("Day") || period.equalsIgnoreCase("Ngày")) return 1;
        if (period.equalsIgnoreCase("Week") || period.equalsIgnoreCase("Tuần")) return 7;
        if (period.equalsIgnoreCase("Month") || period.equalsIgnoreCase("Tháng")) return 30;
        return 1;
    }

    private void loadXValues() {
        // Hiển thị 7 ngày gần nhất bao gồm cả hôm nay
        LocalDate today = LocalDate.now();
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.US);
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.minusDays(6 - i);
            x_values.set(i, date.format(displayFormatter));
            full_dates.set(i, date.format(dbFormatter));
        }
    }
}
