package com.example.finalproject.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.model.HabitDatabaseHelper;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProgressMonthFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProgressMonthFragment extends Fragment {
    private MaterialCalendarView calendar;
    private Set<CalendarDay> habitDays = new HashSet<>();
    private HabitDatabaseHelper databaseHelper;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProgressMonthFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProgressMonthFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProgressMonthFragment newInstance(String param1, String param2) {
        ProgressMonthFragment fragment = new ProgressMonthFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public static ProgressMonthFragment newInstance(String idHabit, String idTaiKhoan, String test) {
        ProgressMonthFragment fragment = new ProgressMonthFragment();
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
    public void getConnection(String userId,String habbitId){
        databaseHelper = HabitDatabaseHelper.getInstance(requireContext());
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress_month, container, false);
        // Lấy dữ liệu
        String idHabit = getArguments().getString("idHabit");
        String idTaiKhoan = getArguments().getString("idTaiKhoan");
        Log.d("idHabit m", idHabit);
        Log.d("idTaiKhoan m", idTaiKhoan);
        calendar = view.findViewById(R.id.calendarView);
        setupCalendar();
        highlightDays();
        // Inflate the layout for this fragment
        return view;
    }

    private void setupCalendar() {
        calendar.setSelectionMode(MaterialCalendarView.SELECTION_MODE_NONE);
        for (CalendarDay day : habitDays) {
            calendar.setDateSelected(day, true);
        }
    }
    public void highlightDays() {
        String idHabit = getArguments().getString("idHabit");
        String idTaiKhoan = getArguments().getString("idTaiKhoan");
        getConnection(idTaiKhoan,idHabit);
        habitDays.clear();
        databaseHelper.getHabitActions(idTaiKhoan, idHabit).forEach(action -> {
            java.util.Calendar calendarValue = databaseHelper.parseActionTime(action.getActionTime());
            if (calendarValue != null) {
                habitDays.add(CalendarDay.from(calendarValue.get(java.util.Calendar.YEAR), calendarValue.get(java.util.Calendar.MONTH) + 1, calendarValue.get(java.util.Calendar.DAY_OF_MONTH)));
            }
        });
        setupCalendar();
    }
}