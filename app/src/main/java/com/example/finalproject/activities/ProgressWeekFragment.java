package com.example.finalproject.activities;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.viewpager.widget.ViewPager;

import com.example.finalproject.R;
import com.example.finalproject.model.HabitWeek;
import com.example.finalproject.ui.MyPagerProgressAdapter;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.example.finalproject.model.Habit;
import com.example.finalproject.model.HabitDatabaseHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.time.LocalDate;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProgressWeekFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProgressWeekFragment extends Fragment {
    private HabitDatabaseHelper databaseHelper;
    private HabitWeek habitWeek = new HabitWeek();
    private BarChart barChart;
    private List<String> x_values = Arrays.asList("", "", "", "", "", "", "");
    private List<String> x_values_day = Arrays.asList("", "", "", "", "", "", "");

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProgressWeekFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProgressWeekFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProgressWeekFragment newInstance(String param1, String param2) {
        ProgressWeekFragment fragment = new ProgressWeekFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress_week, container, false);
        // Lấy dữ liệu
        String idHabit = getArguments().getString("idHabit");
        String idTaiKhoan = getArguments().getString("idTaiKhoan");
        Log.d("idHabit w", idHabit);
        Log.d("idTaiKhoan w", idTaiKhoan);

        // Load x_values
        loadXValues();

        barChart = view.findViewById(R.id.barChart);
        loadBarChartData(idHabit, idTaiKhoan);
        setUpBarChart();


        // Inflate the layout for this fragment
        return view;
    }

    private void setUpBarChart() {
        barChart.getAxisRight().setDrawLabels(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setMaxVisibleValueCount(50);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(true);
        barChart.setDrawBorders(true);
        barChart.setBorderColor(getResources().getColor(R.color.Blue));
    }

    private void loadBarChartData(String idHabit, String idTaiKhoan) {
        databaseHelper = HabitDatabaseHelper.getInstance(requireContext());
        Habit habit = databaseHelper.getHabit(idTaiKhoan, idHabit);
        if (habit == null) {
            Toast.makeText(getContext(), "Không tìm thấy dữ liệu", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double muctieu = habit.getMucTieu();
            habitWeek.setMucTieu(muctieu);
            String thoiGianBatDau = habit.getThoiGianBatDau();
            String thoiGianKetThuc = habit.getThoiGianKetThuc();
            int thoiGianThucHien = getThoiGianThucHien(thoiGianBatDau, thoiGianKetThuc);
            habitWeek.setSoNgayThucHien(thoiGianThucHien);
            String khoangThoiGian = habit.getKhoangThoiGian();
            int khoangThoiGianInt = getKhoangThoiGian(khoangThoiGian);
            habitWeek.setKhoangThoiGian(khoangThoiGianInt);
            habitWeek.setTrangThai(habit.getTrangThai());
            double donViTang = habit.getDonViTang();
            habitWeek.setDonViTang(donViTang);
            float mucTieuNgay = calculateMaxAxis(muctieu, khoangThoiGianInt);
            habitWeek.setMucTieuNgay(mucTieuNgay);

            ArrayList<BarEntry> barEntriesArrayList = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                if (x_values.get(i).equals("")) {
                    barEntriesArrayList.add(new BarEntry(i, null));
                } else {
                    int ngay = Integer.parseInt(x_values_day.get(i));
                    float percentDay = calculateIncrease(idTaiKhoan, idHabit, mucTieuNgay, ngay, donViTang);
                    barEntriesArrayList.add(new BarEntry(i, percentDay));
                }
            }
            YAxis yAxis = barChart.getAxisLeft();
            yAxis.setAxisMinimum(0);

            yAxis.setAxisMaximum(100);
            yAxis.setGranularity(1f);
            yAxis.setLabelCount(10);

            BarDataSet barDataSet = new BarDataSet(barEntriesArrayList, "Weekly Progress (%)");
            int color = getResources().getColor(R.color.Blue);
            barDataSet.setColor(color);

            BarData barData = new BarData(barDataSet);
            barChart.setData(barData);
            barChart.getDescription().setEnabled(false);
            barChart.invalidate();

            barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(x_values));
            barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            barChart.getXAxis().setGranularity(1f);
            barChart.getXAxis().setGranularityEnabled(true);
        }
        catch (Exception e) {
            Log.d("Error", e.getMessage());
        }


    }
    private int getThoiGianThucHien(String thoiGianBatDau, String thoiGianKetThuc) {
        // thoiGianBatDau và thoiGianKetThuc có dạng "dd-MM-yyyy"
        String[] thoiGianBatDauArr = thoiGianBatDau.split("-");
        String[] thoiGianKetThucArr = thoiGianKetThuc.split("-");
        LocalDate ngayBatDau = LocalDate.of(Integer.parseInt(thoiGianBatDauArr[2]), Integer.parseInt(thoiGianBatDauArr[1]), Integer.parseInt(thoiGianBatDauArr[0]));
        LocalDate ngayKetThuc = LocalDate.of(Integer.parseInt(thoiGianKetThucArr[2]), Integer.parseInt(thoiGianKetThucArr[1]), Integer.parseInt(thoiGianKetThucArr[0]));
        int thoiGianThucHien = ngayKetThuc.getDayOfYear() - ngayBatDau.getDayOfYear() + 1;
        return thoiGianThucHien;
    }
    private float calculateMaxAxis(double mucTieu, int thoiGianThucHien) {
        //int maxAxis = (int) Math.ceil(mucTieu*1.0 / thoiGianThucHien);
        double result = mucTieu * 1.0 / thoiGianThucHien;
        double roundedResult = Math.round(result * 100.0) / 100.0;
        Log.d("roundedResult", String.valueOf(roundedResult));
        return (float) roundedResult;
    }
    private int getKhoangThoiGian(String khoangThoiGian) {
        if (khoangThoiGian.equals("Day")) {
            return 1;
        } else if (khoangThoiGian.equals("Week")) {
            return 7;
        } else if (khoangThoiGian.equals("Month")) {
            return 30;
        }
        return 0;
    }
    // Hàm tính toán độ tăng của một thói quen trong ngày
    private float calculateIncrease(String idTaiKhoan, String idHabit, float mucTieuNgay, int ngay, double donViTang) {
        double value = databaseHelper.getCurrentMonthActionCount(idTaiKhoan, idHabit, ngay);
        double increase = Math.round((value * 100.0 * 100.0 / mucTieuNgay) / 100.0);
        return (float) increase;
    }


    // Hàm lấy ngày hiện tại trong tuần cho vào x_values
    private void loadXValues() {
        int currentDay = LocalDateTime.now().getDayOfMonth();
        int currentMonth = LocalDateTime.now().getMonthValue();
        //int currentDay = 9;
        Log.d("currentDayIndex", String.valueOf(currentDay));
        if (currentDay > 7) {
            for (int i = 0; i < 7; i++) {
                int day = currentDay - i;
                if (day <= 0) {
                    day += 30;
                }
                x_values_day.set(6 - i, String.valueOf(day));
                String dayMonth = day + "/" + currentMonth;
                x_values.set(6 - i, dayMonth);
            }
        } else if (currentDay <= 7) {
            for (int i = 1; i < currentDay + 1; i++) {
                x_values_day.set(i-1, String.valueOf(i));
                String dayMonth = i + "/" + currentMonth;
                x_values.set(i-1, dayMonth);
            }
            for (int i = currentDay +1; i < 8; i++) {
                x_values_day.set(i-1, "");
                x_values.set(i-1, "");
            }
        }
        Log.d("x_values", x_values.toString());
        Log.d("x_values_day", x_values_day.toString());

    }
    public void getConnection(String idUser, String idHabit) {
        databaseHelper = HabitDatabaseHelper.getInstance(requireContext());
    }
}