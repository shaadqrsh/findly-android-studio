package com.example.amp_mini_project.Activities;

import android.os.Bundle;
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
        TextView textView = findViewById(R.id.text_found);
        textView.setTextColor(getResources().getColor(R.color.myPrimary));
        ImageView imageView = findViewById(R.id.icon_found);
        imageView.setColorFilter(getResources().getColor(R.color.myPrimary));
        button.setOnClickListener(null);
    }
}