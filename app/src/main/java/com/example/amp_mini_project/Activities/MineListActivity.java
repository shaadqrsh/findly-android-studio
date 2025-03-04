package com.example.amp_mini_project.Activities;


import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.amp_mini_project.R;

public class MineListActivity extends ListActivity {
    protected void onCreate(Bundle savedInstanceState) {
        typeFilter = -1;
        setContentView(R.layout.activity_mine_list);
        super.onCreate(savedInstanceState);
    }

    protected void setupBottomNavigation() {
        super.setupBottomNavigation();
        LinearLayout button = findViewById(R.id.button_mine);
        button.setBackgroundColor(getColor(R.color.mySecondary));

        TextView textView = findViewById(R.id.text_mine);
        textView.setTextColor(getResources().getColor(R.color.myPrimary));

        ImageView imageView = findViewById(R.id.icon_mine);
        imageView.setColorFilter(getResources().getColor(R.color.myPrimary));
        button.setOnClickListener(null);
    }
}