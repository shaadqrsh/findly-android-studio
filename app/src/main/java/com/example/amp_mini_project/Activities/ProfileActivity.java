package com.example.findly.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.findly.Firebase.DatabaseUser;
import com.example.findly.Helpers.ImageHelper;
import com.example.findly.Helpers.MyApp;
import com.example.findly.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView usernameTextView, nameTextView, emailTextView, phoneTextView;
    private EditText nameEditText, emailEditText, phoneEditText;
    private Button changeProfilePicButton, editButton, logoutButton;
    private Uri imageUri;
    private DatabaseReference usersRef;
    private StorageReference storageRef;
    private String userId;
    private String profileUri;

    private String currentPassword;

    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        MyApp app = (MyApp) getApplication();
        userId = app.getUserId(); // Retrieve the username as the key from app

        // Initialize Views
        profileImageView = findViewById(R.id.profileImageView);
        usernameTextView = findViewById(R.id.usernameTextView);
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        changeProfilePicButton = findViewById(R.id.changeProfilePicButton);
        editButton = findViewById(R.id.editButton);
        logoutButton = findViewById(R.id.logoutButton);

        changeProfilePicButton.setVisibility(View.GONE);
        nameEditText.setVisibility(View.GONE);
        emailEditText.setVisibility(View.GONE);
        phoneEditText.setVisibility(View.GONE);

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setVisibility(View.GONE);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        storageRef = FirebaseStorage.getInstance().getReference("profile_pictures");

        loadUserData();

        changeProfilePicButton.setOnClickListener(v -> {
            ImageHelper.showImageSourceDialog(this);
        });

        editButton.setOnClickListener(v -> {
            if (isEditing) {
                updateUserData();
            } else {
                enableEditing();
            }
        });

        logoutButton.setOnClickListener(v -> {
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            app.setUserId("");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isEditing) {
                    disableEditing();
                } else {
                    setEnabled(false);
                    onBackPressedDispatcher.onBackPressed();
                }
            }
        });

        setupBottomNavigation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri resultUri = ImageHelper.handleActivityResult(requestCode, resultCode, data);
        if (resultUri != null) {
            imageUri = resultUri;
            profileImageView.setImageURI(imageUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ImageHelper.handlePermissionResult(requestCode, permissions, grantResults)) {
            ImageHelper.openCamera(this);
        } else if (requestCode == ImageHelper.CAMERA_PERMISSION_REQUEST_CODE) {
            Toast.makeText(this, "Camera permission is required to take pictures", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserData() {
        showLoadingOverlay();
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DatabaseUser user = snapshot.getValue(DatabaseUser.class);
                    if (user != null) {
                        usernameTextView.setText(userId);
                        nameTextView.setText(user.getName());
                        emailTextView.setText(user.getEmail());
                        phoneTextView.setText(user.getPhone());

                        nameEditText.setText(user.getName());
                        emailEditText.setText(user.getEmail());
                        phoneEditText.setText(user.getPhone());

                        profileUri = user.getProfileUri();

                        if (profileUri != null && !profileUri.isEmpty()) {
                            Glide.with(ProfileActivity.this).load(profileUri).into(profileImageView);
                        }
                    }
                }
                hideLoadingOverlay();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
                hideLoadingOverlay();
            }
        });
    }

    private void enableEditing() {
        isEditing = true;
        nameTextView.setVisibility(View.GONE);
        emailTextView.setVisibility(View.GONE);
        phoneTextView.setVisibility(View.GONE);
        nameEditText.setVisibility(View.VISIBLE);
        emailEditText.setVisibility(View.VISIBLE);
        phoneEditText.setVisibility(View.VISIBLE);
        editButton.setText("Submit");
        changeProfilePicButton.setVisibility(View.VISIBLE);
    }

    private void disableEditing() {
        isEditing = false;
        nameTextView.setVisibility(View.VISIBLE);
        emailTextView.setVisibility(View.VISIBLE);
        phoneTextView.setVisibility(View.VISIBLE);
        nameEditText.setVisibility(View.GONE);
        emailEditText.setVisibility(View.GONE);
        phoneEditText.setVisibility(View.GONE);
        editButton.setText("Edit");
        changeProfilePicButton.setVisibility(View.GONE);
    }

    private void updateUserData() {
        String updatedName = nameEditText.getText().toString().trim();
        String updatedEmail = emailEditText.getText().toString().trim();
        String updatedPhone = phoneEditText.getText().toString().trim();

        usersRef.child(userId).child(DatabaseUser.key_password)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        currentPassword = snapshot.getValue(String.class);
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Handle error
                    }
                });

        if (updatedName.isEmpty()) {
            nameEditText.setError("Name cannot be empty");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(updatedEmail).matches()) {
            emailEditText.setError("Invalid email address");
            return;
        }
        if (!Patterns.PHONE.matcher(updatedPhone).matches()) {
            phoneEditText.setError("Phone number must be 10-15 digits long, optionally starting with '+'");
            return;
        }

        showLoadingOverlay();

        Runnable updateDatabase = () -> {
            String profileImageToSave = (imageUri == null) ? profileUri : "";
            DatabaseUser updatedUser = new DatabaseUser(updatedName, updatedPhone, updatedEmail, currentPassword, profileImageToSave);
            usersRef.child(userId).setValue(updatedUser).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Update display fields with new data
                    nameTextView.setText(updatedName);
                    emailTextView.setText(updatedEmail);
                    phoneTextView.setText(updatedPhone);
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    disableEditing();
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
                }
                hideLoadingOverlay();
            });
        };

        if (imageUri != null) {
            StorageReference fileRef = storageRef.child(userId + ".jpg");
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        profileUri = imageUrl;
                        DatabaseUser updatedUser = new DatabaseUser(updatedName, updatedPhone, updatedEmail, currentPassword, imageUrl);
                        usersRef.child(userId).setValue(updatedUser).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Update display fields with new data and refresh the profile image
                                nameTextView.setText(updatedName);
                                emailTextView.setText(updatedEmail);
                                phoneTextView.setText(updatedPhone);
                                Glide.with(ProfileActivity.this).load(profileUri).into(profileImageView);
                                Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                disableEditing();
                            } else {
                                Toast.makeText(ProfileActivity.this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
                            }
                            hideLoadingOverlay();
                        });
                    })).addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
                        hideLoadingOverlay();
                    });
        } else {
            updateDatabase.run();
        }
    }

    private void showLoadingOverlay() {
        findViewById(R.id.loading_overlay).setVisibility(View.VISIBLE);
        findViewById(R.id.loading_overlay).setClickable(true);
    }

    private void hideLoadingOverlay() {
        findViewById(R.id.loading_overlay).setVisibility(View.GONE);
        findViewById(R.id.loading_overlay).setClickable(false);
    }

    protected void setupBottomNavigation() {
        LinearLayout lostButton = findViewById(R.id.button_lost);
        LinearLayout foundButton = findViewById(R.id.button_found);
        LinearLayout mineButton = findViewById(R.id.button_mine);
        LinearLayout messagesButton = findViewById(R.id.button_messages);

        lostButton.setOnClickListener(v -> navigateTo(LostListActivity.class));
        foundButton.setOnClickListener(v -> navigateTo(FoundListActivity.class));
        mineButton.setOnClickListener(v -> navigateTo(MineListActivity.class));
        messagesButton.setOnClickListener(v -> navigateTo(MessagesActivity.class));

        LinearLayout profileButton = findViewById(R.id.button_profile);
        profileButton.setBackgroundColor(getColor(R.color.mySecondary));

        TextView textView = findViewById(R.id.text_profile);
        textView.setTextColor(getResources().getColor(R.color.myPrimary));

        ImageView imageView = findViewById(R.id.icon_profile);
        imageView.setColorFilter(getResources().getColor(R.color.myPrimary));
    }

    protected void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}