package com.example.findly.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.example.findly.Helpers.MyApp;
import com.example.findly.R;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyApp app = (MyApp) getApplication();
        // app.setUserId("shaad");
        new Handler().postDelayed(() -> {
            Intent intent;
            if (app.getUserId() == null) {
                intent = new Intent(MainActivity.this, LoginActivity.class);
            }
            else {
                intent = new Intent(MainActivity.this, LostListActivity.class);
            }
            startActivity(intent);
            finish();
        }, 3000);
    }
}
