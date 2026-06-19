package com.example.finalproject.activities;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.finalproject.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.example.finalproject.model.Habit;
import com.example.finalproject.model.HabitDatabaseHelper;
import android.graphics.Color;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;

public class ProgressDayFragment extends Fragment {
    private PieChart pieChart;
    private HabitDatabaseHelper databaseHelper;
    private TextView tvTodayProgress;
    private TextView tvCurrentProgress;
    private TextView tvProgressPercent;

    public ProgressDayFragment() {
    }

    public static ProgressDayFragment newInstance(String idHabit, String idTaiKhoan, String test) {
        ProgressDayFragment fragment = new ProgressDayFragment();
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
        View view = inflater.inflate(R.layout.fragment_progress_day, container, false);
        String idHabit = getArguments().getString("idHabit");
        String idTaiKhoan = getArguments().getString("idTaiKhoan");

        pieChart = view.findViewById(R.id.pieChart);
        tvTodayProgress = view.findViewById(R.id.tvTodayProgress);
        tvCurrentProgress = view.findViewById(R.id.tvCurrentProgress);
        tvProgressPercent = view.findViewById(R.id.tv_progress_percent);

        setUpPieChart();
        getProgressDay(idTaiKhoan, idHabit);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        String idHabit = getArguments().getString("idHabit");
        String idTaiKhoan = getArguments().getString("idTaiKhoan");
        if (idHabit != null && idTaiKhoan != null) {
            getProgressDay(idTaiKhoan, idHabit);
        }
    }

    private void getProgressDay(String idTaiKhoan, String idHabit) {
        databaseHelper = HabitDatabaseHelper.getInstance(requireContext());
        Habit habit = databaseHelper.getHabit(idTaiKhoan, idHabit);
        if (habit == null) return;

        try {
            // Sử dụng mục tiêu gốc, không chia nhỏ
            double target = habit.getMucTieu();
            String donvi = habit.getDonVi();
            
            // Đồng bộ đơn vị
            String unitVn = donvi;
            if ("hours".equalsIgnoreCase(unitVn)) unitVn = "Giờ";
            else if ("pages".equalsIgnoreCase(unitVn)) unitVn = "Trang";

            tvTodayProgress.setText("Mục tiêu: " + String.format(Locale.getDefault(), "%.1f", target) + " " + unitVn);
            
            // Lấy tiến độ tương ứng với tần suất (để khớp với màn hình chính)
            double currentProgress = getProgressByPeriod(idHabit, habit.getKhoangThoiGian());
            
            tvCurrentProgress.setText(String.format(Locale.getDefault(), "%.1f", currentProgress) + " " + unitVn);
            
            double percent = (target > 0) ? (currentProgress * 100.0 / target) : 0;
            if (tvProgressPercent != null) {
                tvProgressPercent.setText(String.format(Locale.getDefault(), "Đã hoàn thành %.0f%%", percent));
            }

            loadPieChartData(currentProgress, target);
        } catch (Exception e) {
            Log.e("ProgressDay", "Error: " + e.getMessage());
        }
    }

    private double getProgressByPeriod(String habitId, String period) {
        if (period == null) period = "Ngày";
        String idUser = getArguments().getString("idTaiKhoan");
        
        if (period.equalsIgnoreCase("Day") || period.equalsIgnoreCase("Ngày")) {
            return databaseHelper.getTodayProgress(idUser, habitId);
        } else if (period.equalsIgnoreCase("Week") || period.equalsIgnoreCase("Tuần")) {
            // Tính tổng 7 ngày gần đây
            return calculateRecentProgress(idUser, habitId, 7);
        } else if (period.equalsIgnoreCase("Month") || period.equalsIgnoreCase("Tháng")) {
            // Tính tổng 30 ngày gần đây
            return calculateRecentProgress(idUser, habitId, 30);
        }
        return databaseHelper.getTodayProgress(idUser, habitId);
    }

    private double calculateRecentProgress(String uid, String hid, int days) {
        java.util.List<com.example.finalproject.model.HabitAction> actions = databaseHelper.getHabitActions(uid, hid);
        double sum = 0;
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DATE, -days);
        java.util.Date startDate = cal.getTime();
        
        for (com.example.finalproject.model.HabitAction action : actions) {
            java.util.Calendar actionTime = databaseHelper.parseActionTime(action.getActionTime());
            if (actionTime != null && actionTime.getTime().after(startDate)) {
                sum += action.getValue();
            }
        }
        return sum;
    }

    private void setUpPieChart() {
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.getLegend().setEnabled(false);
    }

    private void loadPieChartData(double current, double target) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        float percent = (target > 0) ? (float)(current / target * 100) : 0f;
        
        if (percent >= 100) {
            entries.add(new PieEntry(100f, ""));
        } else {
            entries.add(new PieEntry(percent, ""));
            entries.add(new PieEntry(100f - percent, ""));
        }

        PieDataSet dataset = new PieDataSet(entries, "");
        dataset.setSliceSpace(3f);
        dataset.setDrawValues(false);
        int[] colors = {getResources().getColor(R.color.Blue), Color.parseColor("#E0E0E0")};
        dataset.setColors(colors);

        PieData data = new PieData(dataset);
        pieChart.setData(data);
        pieChart.invalidate();
    }
}
