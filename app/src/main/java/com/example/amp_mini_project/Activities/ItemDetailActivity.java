package com.example.amp_mini_project.Activities;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.target.Target;
import com.example.amp_mini_project.Firebase.DatabaseItem;
import com.example.amp_mini_project.Firebase.DatabaseUser;
import com.example.amp_mini_project.Helpers.MyApp;
import com.example.amp_mini_project.R;
import com.example.amp_mini_project.Helpers.UserDataCallback;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.amp_mini_project.Helpers.ImageHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ItemDetailActivity extends AppCompatActivity {

    private TextView itemName, itemCategory, uploadedBy, uploadDateTime, itemDescription;
    private EditText itemNameEdit, itemDescriptionEdit;
    private Spinner itemCategorySpinner;
    private Button contactButton, statusToggleButton, editItemButton, uploadButton;
    private ImageView itemImage, profileIcon;
    private View loadingOverlay;
    private boolean isImageLoaded = false;
    private boolean isUsernameLoaded = false;
    private boolean isProfilePicLoaded = false;
    private boolean isEditing = false;
    private DatabaseItem currentItem;
    private String currentItemKey;
    private Uri imageUri;
    private String currentImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        // Initialize Views
        itemName = findViewById(R.id.item_name);
        itemCategory = findViewById(R.id.item_category);
        itemDescription = findViewById(R.id.item_description);
        itemNameEdit = findViewById(R.id.item_name_edit);
        itemDescriptionEdit = findViewById(R.id.item_description_edit);
        itemCategorySpinner = findViewById(R.id.item_category_spinner);
        uploadedBy = findViewById(R.id.uploaded_by);
        uploadDateTime = findViewById(R.id.upload_date_time);
        contactButton = findViewById(R.id.contact_button);
        statusToggleButton = findViewById(R.id.mark_button);
        editItemButton = findViewById(R.id.edit_item_button);
        uploadButton = findViewById(R.id.upload_image_button);
        itemImage = findViewById(R.id.item_image);
        profileIcon = findViewById(R.id.profile_icon);
        loadingOverlay = findViewById(R.id.loading_overlay);

        // Initially hide the edit button; will be shown for uploader
        editItemButton.setVisibility(View.GONE);

        // Get itemKey from Intent
        Intent intent = getIntent();
        currentItemKey = intent.getStringExtra("itemKey");

        if (currentItemKey != null) {
            loadItemDetails(currentItemKey);
        }
        contactButton.setOnClickListener(v -> {
            Intent intent2 = new Intent(ItemDetailActivity.this, ContactActivity.class);
            intent2.putExtra("itemKey", currentItemKey);
            startActivity(intent2);
        });

        statusToggleButton.setOnClickListener(v -> toggleItemStatus(currentItemKey));

        editItemButton.setOnClickListener(v -> {
            if (isEditing) {
                updateItemData();
            } else {
                enableEditing();
            }
        });

        uploadButton.setOnClickListener(v -> {
            ImageHelper.showImageSourceDialog(this);
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ImageHelper.handlePermissionResult(requestCode, permissions, grantResults)) {
            ImageHelper.openCamera(this);
        } else if (requestCode == ImageHelper.CAMERA_PERMISSION_REQUEST_CODE) {
            Toast.makeText(this, "Camera permission is required to take pictures", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri resultUri = ImageHelper.handleActivityResult(requestCode, resultCode, data);
        if (resultUri != null) {
            imageUri = resultUri;
            itemImage.setImageURI(imageUri);
        }
    }

    private void loadItemDetails(String itemKey) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("entries").child(itemKey);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    currentItem = snapshot.getValue(DatabaseItem.class);
                    if(currentItem != null){
                        // Populate view TextViews using DatabaseItem fields
                        itemName.setText(currentItem.getName());
                        itemCategory.setText(currentItem.getCategory());
                        itemDescription.setText(currentItem.getDescription());
                        uploadDateTime.setText(currentItem.displayDate() + " " + currentItem.displayTime());

                        // Pre-populate EditTexts
                        itemNameEdit.setText(currentItem.getName());
                        itemDescriptionEdit.setText(currentItem.getDescription());
                        // Setup Spinner: load array from resources and pre-select current category
                        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ItemDetailActivity.this,
                                R.array.categories, android.R.layout.simple_spinner_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        itemCategorySpinner.setAdapter(adapter);
                        int spinnerPosition = adapter.getPosition(currentItem.getCategory());
                        itemCategorySpinner.setSelection(spinnerPosition);

                        // Load the item image and store the current image URL
                        if(currentItem.getImageUrl() != null && !currentItem.getImageUrl().isEmpty()){
                            currentImageUrl = currentItem.getImageUrl();
                            Glide.with(ItemDetailActivity.this)
                                    .load(currentImageUrl)
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

                        // Load uploader details
                        currentItem.getUserData(DatabaseUser.key_name, "Unknown User", new UserDataCallback() {
                            @Override
                            public void onUserDataRetrieved(String name) {
                                uploadedBy.setText(name);
                                isUsernameLoaded = true;
                                checkIfLoadingComplete();
                            }
                        });

                        currentItem.getUserData("profileImage", "", new UserDataCallback() {
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

                        if(userId.equals(currentItem.getUploaderId())){
                            contactButton.setVisibility(View.GONE);
                            statusToggleButton.setVisibility(View.VISIBLE);
                            editItemButton.setVisibility(View.VISIBLE);
                            changeToggleText(currentItem.getStatus());
                        } else {
                            contactButton.setVisibility(View.VISIBLE);
                            statusToggleButton.setVisibility(View.GONE);
                            editItemButton.setVisibility(View.GONE);
                        }
                    }
                }
                hideLoadingOverlay();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideLoadingOverlay();
            }
        });
    }

    private void enableEditing() {
        isEditing = true;
        itemName.setVisibility(View.GONE);
        itemCategory.setVisibility(View.GONE);
        itemDescription.setVisibility(View.GONE);
        itemNameEdit.setVisibility(View.VISIBLE);
        itemCategorySpinner.setVisibility(View.VISIBLE);
        itemDescriptionEdit.setVisibility(View.VISIBLE);
        uploadButton.setVisibility(View.VISIBLE);
        editItemButton.setText("Submit");
    }

    private void disableEditing() {
        isEditing = false;
        itemName.setVisibility(View.VISIBLE);
        itemCategory.setVisibility(View.VISIBLE);
        itemDescription.setVisibility(View.VISIBLE);
        itemNameEdit.setVisibility(View.GONE);
        itemCategorySpinner.setVisibility(View.GONE);
        itemDescriptionEdit.setVisibility(View.GONE);
        uploadButton.setVisibility(View.GONE);
        editItemButton.setText("Edit");
    }

    private void updateItemData() {
        String newName = itemNameEdit.getText().toString().trim();
        String newCategory = itemCategorySpinner.getSelectedItem().toString();
        String newDescription = itemDescriptionEdit.getText().toString().trim();

        if (newName.isEmpty()){
            itemNameEdit.setError("Name cannot be empty");
            return;
        }
        if (newCategory.isEmpty()){
            Toast.makeText(this, "Category cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newDescription.isEmpty()){
            itemDescriptionEdit.setError("Description cannot be empty");
            return;
        }

        // Update the currentItem object with the new text fields.
        currentItem.setName(newName);
        currentItem.setCategory(newCategory);
        currentItem.setDescription(newDescription);

        showLoadingOverlay();

        // If a new image has been selected, upload it first.
        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("item_images");
            StorageReference fileRef = storageRef.child(currentItemKey + ".jpg");
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String newImageUrl = uri.toString();
                        // Update the currentItem image URL with the new URL.
                        currentItem.setImageUrl(newImageUrl);
                        currentImageUrl = newImageUrl;
                        // Now update the database with the new data.
                        updateDatabase();
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(ItemDetailActivity.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
                        hideLoadingOverlay();
                    });
        } else {
            // No new image selected; retain the current image URL.
            currentItem.setImageUrl(currentImageUrl);
            updateDatabase();
        }
    }

    private void updateDatabase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("entries").child(currentItemKey);
        databaseReference.setValue(currentItem)
                .addOnCompleteListener(task -> {
                    hideLoadingOverlay();
                    if(task.isSuccessful()){
                        Toast.makeText(ItemDetailActivity.this, "Item updated successfully", Toast.LENGTH_SHORT).show();
                        // Update the TextViews with new data.
                        itemName.setText(currentItem.getName());
                        itemCategory.setText(currentItem.getCategory());
                        itemDescription.setText(currentItem.getDescription());
                        disableEditing();
                    } else {
                        Toast.makeText(ItemDetailActivity.this, "Failed to update item", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void toggleItemStatus(String itemKey) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("entries").child(itemKey);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    DatabaseItem item = snapshot.getValue(DatabaseItem.class);
                    if(item != null){
                        int newStatus = (item.getStatus() == 0) ? 1 : 0;
                        item.setStatus(newStatus);
                        databaseReference.setValue(item).addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                changeToggleText(newStatus);
                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    void changeToggleText(int status) {
        if (status == 0) {
            statusToggleButton.setText("Mark as Closed");
        } else {
            statusToggleButton.setText("Mark as Open");
        }
    }

    private void checkIfLoadingComplete() {
        if(isImageLoaded && isUsernameLoaded && isProfilePicLoaded){
            hideLoadingOverlay();
        }
    }

    private void showLoadingOverlay() {
        loadingOverlay.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideLoadingOverlay() {
        loadingOverlay.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
