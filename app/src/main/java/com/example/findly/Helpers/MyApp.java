package com.example.findly.Helpers;

public class MyApp extends android.app.Application {
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
