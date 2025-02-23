package com.example.amp_mini_project;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.amp_mini_project.Activities.FoundListActivity;
import com.example.amp_mini_project.Activities.LostListActivity;
import com.example.amp_mini_project.Activities.MineListActivity;
import com.example.amp_mini_project.Firebase.DatabaseUser;
import com.example.amp_mini_project.Helpers.ImageHelper;
import com.example.amp_mini_project.Helpers.MyApp;
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
    private Button changeProfilePicButton, editButton;
    private Uri imageUri;

    // New field for camera image Uri
    private Uri photoUri;

    private DatabaseReference usersRef;
    private StorageReference storageRef;
    private String userId;

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

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setVisibility(View.GONE);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        storageRef = FirebaseStorage.getInstance().getReference("profile_pictures");

        loadUserData();

        changeProfilePicButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Image Source")
                    .setItems(new CharSequence[]{"Gallery", "Camera"}, (dialog, which) -> {
                        if (which == 0) {
                            ImageHelper.openFileChooser(this);
                        } else if (which == 1) {
                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                    != PackageManager.PERMISSION_GRANTED) {
                                Log.i("AAAA", "AAAA");
                                ActivityCompat.requestPermissions(this,
                                        new String[]{Manifest.permission.CAMERA},
                                        ImageHelper.CAMERA_PERMISSION_REQUEST_CODE);
                            } else {
                                ImageHelper.openCamera(this);
                            }
                        }
                    });
            builder.create().show();
        });

        editButton.setOnClickListener(v -> {
            if (isEditing) {
                updateUserData();
            } else {
                enableEditing();
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
            showLoadingOverlay();
            uploadProfilePicture();
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

    private void uploadProfilePicture() {
        if (imageUri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }
        StorageReference fileRef = storageRef.child(userId + ".jpg");
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    usersRef.child(userId).child("profileImage").setValue(imageUrl)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                                hideLoadingOverlay();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to update profile picture.", Toast.LENGTH_SHORT).show();
                                hideLoadingOverlay();
                            });
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Image upload failed.", Toast.LENGTH_SHORT).show();
                    hideLoadingOverlay();
                });
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

                        if (user.getProfileUri() != null && !user.getProfileUri().isEmpty()) {
                            Glide.with(ProfileActivity.this).load(user.getProfileUri()).into(profileImageView);
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
    }

    private void updateUserData() {
        String updatedName = nameEditText.getText().toString().trim();
        String updatedEmail = emailEditText.getText().toString().trim();
        String updatedPhone = phoneEditText.getText().toString().trim();

        if (updatedName.isEmpty()) {
            nameEditText.setError("Name cannot be empty");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(updatedEmail).matches()) {
            emailEditText.setError("Invalid email address");
            return;
        }
        if (!updatedPhone.matches("\\d{10,15}")) {
            phoneEditText.setError("Phone number must be 10-15 digits long");
            return;
        }

        showLoadingOverlay();
        DatabaseUser updatedUser = new DatabaseUser(updatedName, updatedPhone, updatedEmail, "");
        usersRef.child(userId).setValue(updatedUser).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                isEditing = false;
                editButton.setText("Edit");
            } else {
                Toast.makeText(ProfileActivity.this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
            }
            hideLoadingOverlay();
        });
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
        // messagesButton.setOnClickListener(v -> navigateTo(MessagesActivity.class));

        LinearLayout profileButton = findViewById(R.id.button_profile);
        profileButton.setBackgroundColor(getColor(R.color.mySecondary));

        TextView textView = findViewById(R.id.text_profile);
        textView.setTextColor(getResources().getColor(R.color.black));

        ImageView imageView = findViewById(R.id.icon_profile);
        imageView.setColorFilter(getResources().getColor(R.color.black));
    }

    protected void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}
