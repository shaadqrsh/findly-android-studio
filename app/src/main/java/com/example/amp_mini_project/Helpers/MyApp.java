package com.example.amp_mini_project.Helpers;

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
