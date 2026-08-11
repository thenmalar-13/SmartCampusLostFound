package com.example.smartcampuslostfound;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class DashboardActivity extends AppCompatActivity {

    private MaterialCardView cardViewItems, cardReportItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        cardViewItems = findViewById(R.id.cardViewItems);
        cardReportItem = findViewById(R.id.cardReportItem);

        // Navigate to View Items Screen
        cardViewItems.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ViewItemsActivity.class);
            startActivity(intent);
        });

        // Navigate to Report Item Screen
        cardReportItem.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ReportLostActivity.class);
            startActivity(intent);
        });
    }
}
