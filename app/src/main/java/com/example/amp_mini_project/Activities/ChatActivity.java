package com.example.amp_mini_project.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amp_mini_project.Firebase.DatabaseItem;
import com.example.amp_mini_project.Firebase.DatabaseMessage;
import com.example.amp_mini_project.Firebase.DatabaseMessageAdapter;
import com.example.amp_mini_project.Helpers.MyApp;
import com.example.amp_mini_project.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText chatInput;
    private Button sendButton;
    private CheckBox sendPhoneCheckbox, sendEmailCheckbox;
    private DatabaseMessageAdapter chatAdapter;
    private List<DatabaseMessage> messageList;

    private String currentUserId;

    private DatabaseItem currentItem;
    private String currentItemKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        chatInput = findViewById(R.id.chat_input);
        sendButton = findViewById(R.id.send_button);
        sendPhoneCheckbox = findViewById(R.id.send_phone_checkbox);
        sendEmailCheckbox = findViewById(R.id.send_email_checkbox);

        MyApp app = (MyApp) getApplication();
        currentUserId = app.getUserId();

        currentItemKey = getIntent().getStringExtra("item_id");

        if (currentItemKey != null) {
            loadItemDetails(currentItemKey);
        } else {
            Toast.makeText(this, "No item id provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        messageList = new ArrayList<>();
        chatAdapter = new DatabaseMessageAdapter(messageList, currentUserId);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String text = chatInput.getText().toString().trim();
                if (text.isEmpty() || currentItem == null) {
                    return;
                }
                DatabaseMessage message = new DatabaseMessage(
                        currentUserId,
                        currentItem.getUploaderId(),
                        currentItemKey,
                        text,
                        System.currentTimeMillis(),
                        false,
                        sendPhoneCheckbox.isChecked(),
                        sendEmailCheckbox.isChecked()
                );

                DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages");
                messagesRef.push().setValue(message)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                chatInput.setText("");
                            } else {
                                Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void loadItemDetails(String itemKey) {
        DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("entries").child(itemKey);
        itemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    currentItem = snapshot.getValue(DatabaseItem.class);
                    if (currentItem != null) {
                        currentItem.setKey(snapshot.getKey());
                        TextView itemName = findViewById(R.id.item_name);
                        itemName.setText(currentItem.getName());
                        loadMessages();
                    }
                } else {
                    Toast.makeText(ChatActivity.this, "Item not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Failed to load item details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMessages() {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages");
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    DatabaseMessage message = child.getValue(DatabaseMessage.class);
                    if (message != null && message.getItemId().equals(currentItemKey)) {
                        String senderId = message.getSenderId();
                        String receiverId = message.getReceiverId();
                        boolean currentUserInvolved = senderId.equals(currentUserId) || receiverId.equals(currentUserId);
                        if (currentUserInvolved) {
                            messageList.add(message);
                            if (receiverId.equals(currentUserId) && !message.isRead()) {
                                child.getRef().child("read").setValue(true);
                            }
                        }

                    }
                }
                chatAdapter.setMessages(messageList);
                if (!messageList.isEmpty()) {
                    chatRecyclerView.scrollToPosition(messageList.size() - 1);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
