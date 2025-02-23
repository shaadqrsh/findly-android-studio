package com.example.amp_mini_project.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.amp_mini_project.R;

public class FoundListActivity extends ListActivity {
    protected void onCreate(Bundle savedInstanceState) {
        typeFilter = 1;
        setContentView(R.layout.activity_found_list);
        super.onCreate(savedInstanceState);
    }

    protected void setupBottomNavigation() {
        super.setupBottomNavigation();
        LinearLayout button = findViewById(R.id.button_found);
        button.setBackgroundColor(getColor(R.color.mySecondary));

        // Change text color of the TextView
        TextView textView = findViewById(R.id.text_found);
        textView.setTextColor(getResources().getColor(R.color.black));

        // Optionally modify the ImageView tint or resource
        ImageView imageView = findViewById(R.id.icon_found);
        imageView.setColorFilter(getResources().getColor(R.color.black));
        button.setOnClickListener(null);
    }
}