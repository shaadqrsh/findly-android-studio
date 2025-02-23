package com.example.amp_mini_project.Activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.target.Target;
import com.example.amp_mini_project.Firebase.DatabaseItem;
import com.example.amp_mini_project.Helpers.MyApp;
import com.example.amp_mini_project.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.amp_mini_project.Helpers.UserDataCallback;
import com.example.amp_mini_project.Firebase.DatabaseUser;

public class ItemDetailActivity extends AppCompatActivity {

    private TextView itemName, itemCategory, uploadedBy, uploadDateTime, itemDescription;
    private Button contactButton, statusToggleButton;
    private ImageView itemImage, profileIcon;
    private View loadingOverlay;
    private boolean isImageLoaded = false;
    private boolean isUsernameLoaded = false;
    private boolean isProfilePicLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        // Initialize Views
        itemName = findViewById(R.id.item_name);
        itemCategory = findViewById(R.id.item_category);
        uploadedBy = findViewById(R.id.uploaded_by);
        uploadDateTime = findViewById(R.id.upload_date_time);
        itemDescription = findViewById(R.id.item_description);
        contactButton = findViewById(R.id.contact_button);
        statusToggleButton = findViewById(R.id.mark_button);
        itemImage = findViewById(R.id.item_image);
        profileIcon = findViewById(R.id.profile_icon);
        loadingOverlay = findViewById(R.id.loading_overlay);

        // Initially hide the statusToggleButton
        statusToggleButton.setVisibility(View.GONE);

        // Get itemKey from Intent
        Intent intent = getIntent();
        String itemKey = intent.getStringExtra("itemKey");

        if (itemKey != null) {
            loadItemDetails(itemKey);
        }

        // Set listeners
        contactButton.setOnClickListener(v -> {
            // Contact logic (e.g., navigate to another activity or show dialog)
        });

        statusToggleButton.setOnClickListener(v -> toggleItemStatus(itemKey));
    }

    private void loadItemDetails(String itemKey) {
        // Disable user interaction
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("entries").child(itemKey);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DatabaseItem item = snapshot.getValue(DatabaseItem.class);

                    if (item != null) {
                        itemName.setText(item.getName());
                        itemCategory.setText(item.getCategory());
                        itemDescription.setText(item.getDescription());
                        uploadDateTime.setText(item.displayDate() +  " " + item.displayTime());

                        // Load the image for the item
                        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                            Glide.with(ItemDetailActivity.this)
                                    .load(item.getImageUrl())
                                    .apply(new RequestOptions()
                                            .placeholder(R.drawable.my_placeholder)
                                            .error(R.drawable.my_error))
                                    .listener(new com.bumptech.glide.request.RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            isImageLoaded = true;
                                            checkIfLoadingComplete();
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            isImageLoaded = true;
                                            checkIfLoadingComplete();
                                            return false;
                                        }
                                    })
                                    .into(itemImage);
                        } else {
                            isImageLoaded = true;
                        }

                        item.getUserData(DatabaseUser.key_name, "Unknown User", new UserDataCallback() {
                            @Override
                            public void onUserDataRetrieved(String name) {
                                uploadedBy.setText(name);
                                isUsernameLoaded = true;
                                checkIfLoadingComplete();
                            }
                        });

                        item.getUserData("profileImage", "", new UserDataCallback() {
                            @Override
                            public void onUserDataRetrieved(String profileImage) {
                                Glide.with(ItemDetailActivity.this)
                                        .load(profileImage)
                                        .apply(new RequestOptions()
                                                .placeholder(R.drawable.my_placeholder)
                                                .error(R.drawable.my_error))
                                        .into(profileIcon);
                                isProfilePicLoaded = true;
                                checkIfLoadingComplete();
                            }
                        });

                        MyApp app = (MyApp) getApplication();
                        String userId = app.getUserId();

                        if (userId.equals(item.getUploaderId())) {
                            contactButton.setVisibility(View.GONE);
                            statusToggleButton.setVisibility(View.VISIBLE);
                            changeToggleText(item.getStatus());
                        } else {
                            contactButton.setVisibility(View.VISIBLE);
                            statusToggleButton.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
                hideLoadingOverlay();
            }
        });
    }

    private void checkIfLoadingComplete() {
        if (isImageLoaded && isUsernameLoaded && isProfilePicLoaded) {
            hideLoadingOverlay();
        }
    }

    private void hideLoadingOverlay() {
        loadingOverlay.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void toggleItemStatus(String itemKey) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("entries").child(itemKey);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DatabaseItem item = snapshot.getValue(DatabaseItem.class);

                    if (item != null) {
                        // Toggle the status
                        int newStatus = (item.getStatus() == 0) ? 1 : 0;

                        // Update the status in the database
                        databaseReference.child("status").setValue(newStatus).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                changeToggleText(newStatus);
                            } else {
                                // Handle update failure (e.g., show a toast)
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors (e.g., show a toast or log)
            }
        });
    }

    void changeToggleText(int status) {
        if (status == 0) {
            statusToggleButton.setText("Mark as Closed");
            uploadedBy.setText("Open");
        } else {
            statusToggleButton.setText("Mark as Open");
            uploadedBy.setText("Closed");
        }
    }
}
