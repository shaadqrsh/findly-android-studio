package com.example.findly.Firebase;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.example.findly.Helpers.UserDataCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseItem {
    private String name;
    private String category;
    private long uploadTime;
    private String uploaderId;
    private int type;
    private int status;
    private String description;
    private String key;
    private String imageUrl;

    public String getName() {
        return name;
    }
    public String getCategory() {
        return category;
    }
    public int getType() {
        return type;
    }
    public String getUploaderId() {
        return uploaderId;
    }
    public long getUploadTime() {
        return uploadTime;
    }
    public int getStatus() {
        return status;
    }
    public String getDescription() {
        return description;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public void setUploadTime(long time) {
        this.uploadTime = time;
    }
    public void setUploaderId(String userId) {
        this.uploaderId = userId;
    }
    public void setType(int type) {
        this.type = type;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String displayDate() {
        Date date = new Date(uploadTime);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return ""; //dateFormat.format(date);
    }

    public String displayTime() {
        Date date = new Date(uploadTime);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("MMM d, HH:mm");
        return timeFormat.format(date);
    }

    public void getUserData(String item, String defaultItem, UserDataCallback callback) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(getUploaderId());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userData = snapshot.child(item).getValue(String.class);
                    callback.onUserDataRetrieved(userData != null ? userData : defaultItem);
                } else {
                    callback.onUserDataRetrieved(defaultItem);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onUserDataRetrieved(defaultItem);
            }
        });
    }

    public DatabaseItem() {
    }
}
