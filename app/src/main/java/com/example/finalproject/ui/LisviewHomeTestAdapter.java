package com.example.finalproject.ui;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.finalproject.R;
import com.example.finalproject.model.ListviewHomeTest;
import com.example.finalproject.model.HabitDatabaseHelper;

import java.util.ArrayList;
import java.util.Locale;

public class LisviewHomeTestAdapter extends ArrayAdapter<ListviewHomeTest> {
    private Activity context;
    private int resourcedId;
    private ArrayList<ListviewHomeTest> listviewHomeTestsArrayList;
    private HabitDatabaseHelper databaseHelper;
    private String idUser;

    public LisviewHomeTestAdapter(ArrayList<ListviewHomeTest> listviewHomeTestsArrayList, Activity context, int resourceID, String idUser) {
        super(context, resourceID, listviewHomeTestsArrayList);
        this.listviewHomeTestsArrayList = listviewHomeTestsArrayList;
        this.context = context;
        this.resourcedId = resourceID;
        this.idUser = idUser;
        this.databaseHelper = HabitDatabaseHelper.getInstance(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(resourcedId, null);
        }

        ListviewHomeTest item = listviewHomeTestsArrayList.get(position);

        TextView nameHabit = convertView.findViewById(R.id.tvHomeListTitle);
        TextView section = convertView.findViewById(R.id.tvHomeListSection);
        TextView timeHabit = convertView.findViewById(R.id.tvHomeListTime);
        TextView doneProgress = convertView.findViewById(R.id.tvDoneProgress);
        ProgressBar progressBar = convertView.findViewById(R.id.pbHomeListProgress);
        
        // Sử dụng View chung để tránh lỗi ép kiểu khi thay đổi layout
        View ibPlus = convertView.findViewById(R.id.ibHomeListPlus);
        View ibMinus = convertView.findViewById(R.id.ibHomeListMinus);

        nameHabit.setText(item.getNameHabit());
        section.setText(item.getSection());
        timeHabit.setText(item.getTimeHabit());
        doneProgress.setText(item.getDoneProgress());
        progressBar.setProgress(item.getDone());

        if (ibPlus != null) {
            ibPlus.setOnClickListener(v -> {
                if ("Đã hoàn thành".equals(item.getStatus())) {
                    Toast.makeText(context, "Thói quen đã hoàn thành", Toast.LENGTH_SHORT).show();
                } else {
                    updateProgress(item, item.getDonViTang());
                }
            });
        }

        if (ibMinus != null) {
            ibMinus.setOnClickListener(v -> {
                if ("Đã hoàn thành".equals(item.getStatus())) {
                    Toast.makeText(context, "Thói quen đã hoàn thành", Toast.LENGTH_SHORT).show();
                } else if (item.getDoing() <= 0) {
                    Toast.makeText(context, "Tiến độ không thể âm", Toast.LENGTH_SHORT).show();
                } else {
                    updateProgress(item, -item.getDonViTang());
                }
            });
        }

        return convertView;
    }

    private void updateProgress(ListviewHomeTest item, double delta) {
        if (idUser == null) return;
        
        databaseHelper.addHabitAction(idUser, item.getHabitId(), delta);
        
        double newDoing = Math.max(0, item.getDoing() + delta);
        item.setDoing(newDoing);
        
        String unit = "";
        try {
            String[] parts = item.getDoneProgress().split(" ");
            unit = parts[parts.length - 1];
        } catch (Exception e) {
            unit = ""; 
        }
        
        item.setDoneProgress(String.format(Locale.getDefault(), "%.1f/%.1f %s", newDoing, item.getTarget(), unit));
        item.setDone((int) Math.min(100, Math.ceil(newDoing * 100.0 / item.getTarget())));
        
        notifyDataSetChanged();
    }
}
