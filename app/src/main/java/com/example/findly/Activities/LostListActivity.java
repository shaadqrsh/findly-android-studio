package com.example.findly.Activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.findly.R;

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
        TextView textView = findViewById(R.id.text_lost);
        textView.setTextColor(getResources().getColor(R.color.myPrimary));
        ImageView imageView = findViewById(R.id.icon_lost);
        imageView.setColorFilter(getResources().getColor(R.color.myPrimary));
        button.setOnClickListener(null);
    }

}