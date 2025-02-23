package com.example.amp_mini_project.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.amp_mini_project.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class LostListActivity extends ListActivity {
    protected void onCreate(Bundle savedInstanceState) {
        typeFilter = 0;
        setContentView(R.layout.activity_lost_list);
        super.onCreate(savedInstanceState);
    }

    protected void setupBottomNavigation() {
        super.setupBottomNavigation();
        LinearLayout button = findViewById(R.id.button_lost);
        button.setBackgroundColor(getColor(R.color.mySecondary));

        // Change text color of the TextView
        TextView textView = findViewById(R.id.text_lost);
        textView.setTextColor(getResources().getColor(R.color.black));

        // Optionally modify the ImageView tint or resource
        ImageView imageView = findViewById(R.id.icon_lost);
        imageView.setColorFilter(getResources().getColor(R.color.black));
        button.setOnClickListener(null);
    }

}