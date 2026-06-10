package com.example.finalproject.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;

import com.example.finalproject.R;
import com.example.finalproject.model.Account;
import com.example.finalproject.model.SongTestGridView;
import com.example.finalproject.ui.SongTestAdapter;

import java.util.ArrayList;

public class SongsActivity extends AppCompatActivity {
    GridView gridView;
    ImageButton ibHome, ibGraph, ibMusic, ibClock, ibSetting;

    private Account acc = new Account();
    private String idUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs);

        // Safely retrieve Intent data
        Bundle b = getIntent().getExtras();
        if (b != null) {
            Account serializableAcc = (Account) b.getSerializable("user_account");
            if (serializableAcc != null) {
                acc = serializableAcc;
            }
        }
        idUser = getIntent().getStringExtra("idTaiKhoan");

        gridView = findViewById(R.id.gridViewSong);

        // Create an ArrayList of SongTestGridView objects
        ArrayList<SongTestGridView> songTestGridViews = new ArrayList<>();
        songTestGridViews.add(new SongTestGridView("Rain", R.drawable.song_sample1, "1"));
        songTestGridViews.add(new SongTestGridView("Lofi", R.drawable.song_sample2, "2"));
        songTestGridViews.add(new SongTestGridView("Chill", R.drawable.song_sample3, "3"));
        songTestGridViews.add(new SongTestGridView("Music Box", R.drawable.song_sample4, "4"));
        songTestGridViews.add(new SongTestGridView("Bolero", R.drawable.song_sample5, "5"));
        songTestGridViews.add(new SongTestGridView("Piano", R.drawable.song_sample6, "6"));
        songTestGridViews.add(new SongTestGridView("Dance", R.drawable.song_sample71, "7"));
        songTestGridViews.add(new SongTestGridView("Guitar", R.drawable.song_sample8, "8"));

        SongTestAdapter SongAdapter = new SongTestAdapter(this, songTestGridViews);
        gridView.setAdapter(SongAdapter);
        gridView.post(() -> expandGridView(gridView, songTestGridViews.size(), 2));

        setupNavigation();
    }

    private void setupNavigation() {
        ibHome = findViewById(R.id.ib_home);
        ibGraph = findViewById(R.id.ib_graph);
        ibMusic = findViewById(R.id.ib_music);
        ibClock = findViewById(R.id.ib_clock);
        ibSetting = findViewById(R.id.ib_settings);

        ibHome.setOnClickListener(v -> navigateTo(Home_Activity.class));
        ibGraph.setOnClickListener(v -> navigateTo(Progress_total.class));
        ibClock.setOnClickListener(v -> navigateTo(Pomorodo.class));
        ibSetting.setOnClickListener(v -> navigateTo(Setting.class));
        
        // Music button is already on this activity, no need to restart it
        ibMusic.setOnClickListener(null); 
    }

    private void navigateTo(Class<?> target) {
        Intent intent = new Intent(this, target);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user_account", acc);
        intent.putExtra("idTaiKhoan", idUser);
        intent.putExtras(bundle);
        startActivity(intent);
        finish(); // Finish current to prevent back-stack buildup
    }

    private void expandGridView(GridView gridView, int itemCount, int numColumns) {
        if (gridView == null || itemCount <= 0 || numColumns <= 0 || gridView.getAdapter() == null) return;

        int rows = (int) Math.ceil(itemCount / (double) numColumns);
        View listItem = gridView.getAdapter().getView(0, null, gridView);
        int width = gridView.getWidth() > 0 ? gridView.getWidth() : gridView.getResources().getDisplayMetrics().widthPixels;
        
        listItem.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        int itemHeight = listItem.getMeasuredHeight();

        int totalHeight = rows * itemHeight;
        totalHeight += gridView.getVerticalSpacing() * Math.max(0, rows - 1);

        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight;
        gridView.setLayoutParams(params);
        gridView.requestLayout();
    }
}