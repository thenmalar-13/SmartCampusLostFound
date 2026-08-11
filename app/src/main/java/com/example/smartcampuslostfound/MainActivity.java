package com.example.smartcampuslostfound;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnReportLost, btnViewFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnReportLost = findViewById(R.id.btnReportLost);
        btnViewFound = findViewById(R.id.btnViewFound);

        btnReportLost.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ReportLostActivity.class));
        });

        btnViewFound.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ViewItemsActivity.class));
        });
    }
}