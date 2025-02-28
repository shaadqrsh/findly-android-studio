package com.example.amp_mini_project.Activities;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.amp_mini_project.Firebase.DatabaseItem;
import com.example.amp_mini_project.Helpers.ImageHelper;
import com.example.amp_mini_project.Helpers.MyApp;
import com.example.amp_mini_project.R;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AddItemActivity extends AppCompatActivity {

    private EditText etName, etDescription;
    private Spinner spinnerCategory;
    private Button btnSubmit, btnUploadImage;
    private RadioGroup radioGroup;
    private RadioButton radioItem1, radioItem2;
    private int itemType;
    private DatabaseReference databaseReference;
    private Uri imageUri;
    private ImageView imgPreview;
    private StorageReference storageReference;
    private String imageUrl;
    private LinearLayout loadingOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        databaseReference = FirebaseDatabase.getInstance().getReference("entries");

        etName = findViewById(R.id.et_name);
        etDescription = findViewById(R.id.et_description);
        spinnerCategory = findViewById(R.id.spinner_category);
        btnSubmit = findViewById(R.id.btn_submit);
        radioGroup = findViewById(R.id.radio_group_type);
        radioItem1 = findViewById(R.id.radio_lost);
        radioItem2 = findViewById(R.id.radio_found);
        loadingOverlay = findViewById(R.id.loading_overlay);

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setVisibility(View.GONE);

        storageReference = FirebaseStorage.getInstance().getReference("item_images");

        imgPreview = findViewById(R.id.img_preview);
        btnUploadImage = findViewById(R.id.btn_upload_image);

        Intent intent = getIntent();
        itemType = intent.getIntExtra("itemType", 0);

        // Setup spinner adapter
        String[] categories = getResources().getStringArray(R.array.categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Set radio button selection based on itemType
        if (itemType == 0) {
            radioItem1.setChecked(true);
        } else if (itemType == 1) {
            radioItem2.setChecked(true);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_lost) {
                itemType = 0; // Lost
            } else if (checkedId == R.id.radio_found) {
                itemType = 1; // Found
            }
        });

        btnSubmit.setOnClickListener(v -> submitData());

        btnUploadImage.setOnClickListener(v -> {
            ImageHelper.showImageSourceDialog(this);
        });

        hideLoadingOverlay();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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
            imgPreview.setImageURI(imageUri);
        }
    }

    private void submitData() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(description)) {
            etDescription.setError("Description is required");
            return;
        }

        if (radioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select an item type", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Please upload an image", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoadingOverlay();
        uploadImageAndSaveData(name, description, category);
    }

    private void uploadImageAndSaveData(String name, String description, String category) {
        if (imageUri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }
        StorageReference fileReference = storageReference.child(System.currentTimeMillis() + ".jpg");
        fileReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                imageUrl = uri.toString();
                saveItemToDatabase(name, description, category);
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                hideLoadingOverlay();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show();
            hideLoadingOverlay();
        });
    }

    private void saveItemToDatabase(String name, String description, String category) {
        MyApp app = (MyApp) getApplication();
        String userId = app.getUserId();

        String entryId = databaseReference.push().getKey();

        if (entryId != null) {
            DatabaseItem databaseItem = new DatabaseItem();
            databaseItem.setName(name);
            databaseItem.setCategory(category);
            databaseItem.setDescription(description);
            databaseItem.setUploadTime(System.currentTimeMillis());
            databaseItem.setUploaderId(userId);
            databaseItem.setStatus(0);
            databaseItem.setType(itemType);
            databaseItem.setImageUrl(imageUrl);

            databaseReference.child(entryId).setValue(databaseItem).addOnCompleteListener(task -> {
                hideLoadingOverlay();
                if (task.isSuccessful()) {
                    Intent intent = new Intent(AddItemActivity.this, ItemDetailActivity.class);
                    intent.putExtra("itemKey", entryId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Failed to submit data", Toast.LENGTH_SHORT).show();
                }
            });
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
