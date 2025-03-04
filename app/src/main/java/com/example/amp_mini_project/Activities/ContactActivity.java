package com.example.amp_mini_project.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.amp_mini_project.Firebase.DatabaseItem;
import com.example.amp_mini_project.Firebase.DatabaseMessage;
import com.example.amp_mini_project.Firebase.DatabaseUser;
import com.example.amp_mini_project.Helpers.MyApp;
import com.example.amp_mini_project.Helpers.UserDataCallback;
import com.example.amp_mini_project.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ContactActivity extends AppCompatActivity {

    private TextView itemName, itemCategory, itemDescription, uploadedBy, uploadDateTime;
    private ImageView itemImage, profileIcon;


    private EditText multilineTextField;
    private CheckBox sendPhoneCheckbox, sendEmailCheckbox;
    private View loadingOverlay;

    private DatabaseItem currentItem;
    private String currentItemKey;
    private String currentImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        itemName = findViewById(R.id.item_name);
        itemCategory = findViewById(R.id.item_category);
        itemDescription = findViewById(R.id.item_description);
        uploadedBy = findViewById(R.id.uploaded_by);
        uploadDateTime = findViewById(R.id.upload_date_time);
        itemImage = findViewById(R.id.item_image);
        profileIcon = findViewById(R.id.profile_icon);
        loadingOverlay = findViewById(R.id.loading_overlay);
        multilineTextField = findViewById(R.id.multiline_text_field);
        sendPhoneCheckbox = findViewById(R.id.send_phone_checkbox);
        sendEmailCheckbox = findViewById(R.id.send_email_checkbox);
        Button sendButton = findViewById(R.id.send_button);
        currentItemKey = getIntent().getStringExtra("itemKey");
        if (currentItemKey != null) {
            loadItemDetails(currentItemKey);
        }
        sendButton.setOnClickListener(v -> sendMessage());
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setVisibility(View.GONE);
    }

    private void loadItemDetails(String itemKey) {
        showLoadingOverlay();
        DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("entries").child(itemKey);
        itemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    currentItem = snapshot.getValue(DatabaseItem.class);
                    if(currentItem != null) {
                        itemName.setText(currentItem.getName());
                        itemCategory.setText(currentItem.getCategory());
                        itemDescription.setText(currentItem.getDescription());
                        uploadDateTime.setText(currentItem.displayDate() + " " + currentItem.displayTime());
                        if(currentItem.getImageUrl() != null && !currentItem.getImageUrl().isEmpty()){
                            currentImageUrl = currentItem.getImageUrl();
                            Glide.with(ContactActivity.this)
                                    .load(currentImageUrl)
                                    .apply(new RequestOptions()
                                            .placeholder(R.drawable.ic_placeholder)
                                            .error(R.drawable.ic_error))
                                    .into(itemImage);
                        }
                        currentItem.getUserData(DatabaseUser.key_name, "Unknown User", new UserDataCallback() {
                            @Override
                            public void onUserDataRetrieved(String name) {
                                uploadedBy.setText(name);
                            }
                        });

                        currentItem.getUserData("profileImage", "", new UserDataCallback() {
                            @Override
                            public void onUserDataRetrieved(String profileImage) {
                                Glide.with(ContactActivity.this)
                                        .load(profileImage)
                                        .apply(new RequestOptions()
                                                .placeholder(R.drawable.ic_placeholder)
                                                .error(R.drawable.ic_error))
                                        .into(profileIcon);
                            }
                        });
                    }
                }
                hideLoadingOverlay();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideLoadingOverlay();
                Toast.makeText(ContactActivity.this, "Failed to load item details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        MyApp app = (MyApp) getApplication();
        String senderId = app.getUserId();
        String receiverId = currentItem != null ? currentItem.getUploaderId() : "";
        String text = multilineTextField.getText().toString().trim();

        if (text.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseMessage message = new DatabaseMessage(
                senderId,
                receiverId,
                currentItemKey,
                text,
                System.currentTimeMillis(),
                false,
                sendPhoneCheckbox.isChecked(),
                sendEmailCheckbox.isChecked()
        );
        showLoadingOverlay();
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages");
        messagesRef.push().setValue(message)
                .addOnCompleteListener(task -> {
                    hideLoadingOverlay();
                    if (task.isSuccessful()) {
                        Toast.makeText(ContactActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ContactActivity.this, ChatActivity.class);
                        intent.putExtra("item_id", currentItemKey);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ContactActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    private void hideLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }


}
