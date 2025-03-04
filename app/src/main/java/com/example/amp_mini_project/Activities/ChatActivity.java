package com.example.amp_mini_project.Activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.amp_mini_project.Firebase.DatabaseItem;
import com.example.amp_mini_project.Firebase.DatabaseMessage;
import com.example.amp_mini_project.Firebase.DatabaseChatAdapter;
import com.example.amp_mini_project.Firebase.DatabaseUser;
import com.example.amp_mini_project.Helpers.MessageHelper;
import com.example.amp_mini_project.Helpers.MyApp;
import com.example.amp_mini_project.Helpers.UserDataCallback;
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
    private DatabaseChatAdapter chatAdapter;
    private ImageView itemImage;
    private List<DatabaseMessage> messageList;
    private String currentUserId, currentItemKey, otherUserId;
    private boolean isChatActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        chatInput = findViewById(R.id.chat_input);
        sendButton = findViewById(R.id.send_button);
        sendPhoneCheckbox = findViewById(R.id.send_phone_checkbox);
        sendEmailCheckbox = findViewById(R.id.send_email_checkbox);
        itemImage = findViewById(R.id.item_image);

        MyApp app = (MyApp) getApplication();
        currentUserId = app.getUserId();

        currentItemKey = getIntent().getStringExtra("item_id");
        otherUserId = getIntent().getStringExtra("other_user");

        if (currentItemKey != null) {
            loadItemDetails(currentItemKey);
        } else {
            Toast.makeText(this, "No item id provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        messageList = new ArrayList<>();
        chatAdapter = new DatabaseChatAdapter(messageList, currentUserId);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = chatInput.getText().toString().trim();
                if (text.isEmpty()) { return; }
                DatabaseMessage message = new DatabaseMessage(
                        currentUserId,
                        otherUserId,
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

    private MessageHelper.MessageUpdateListener messageUpdateListener = updatedMessage -> {
        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).getKey().equals(updatedMessage.getKey())) {
                messageList.set(i, updatedMessage);
                chatAdapter.notifyItemChanged(i);
                break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        MessageHelper.getInstance().addListener(messageUpdateListener);
        MessageHelper.getInstance().startListening(currentItemKey);
        isChatActive = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        MessageHelper.getInstance().removeListener(messageUpdateListener);
        MessageHelper.getInstance().stopListening();
        isChatActive = false;
    }

    private void loadItemDetails(String itemKey) {
        DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("entries").child(itemKey);
        itemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    DatabaseItem currentItem = snapshot.getValue(DatabaseItem.class);
                    if (currentItem != null) {
                        currentItem.setKey(snapshot.getKey());
                        TextView itemName = findViewById(R.id.item_name);
                        currentItem.getUserData(DatabaseUser.key_name, "Unknown User", new UserDataCallback() {
                            @Override
                            public void onUserDataRetrieved(String name) {
                                itemName.setText(currentItem.getName() + " (" + name + ")");
                            }
                        });
                        itemName.setText(currentItem.getName());
                        if(currentItem.getImageUrl() != null && !currentItem.getImageUrl().isEmpty()) {
                            String imageUrl = currentItem.getImageUrl();
                            Glide.with(ChatActivity.this)
                                    .load(imageUrl)
                                    .apply(new RequestOptions()
                                            .placeholder(R.drawable.ic_placeholder)
                                            .error(R.drawable.ic_error))
                                    .listener(new com.bumptech.glide.request.RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            return false;
                                        }
                                    })
                                    .into(itemImage);
                            loadMessages();
                        }
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
                    message.setKey(child.getKey());
                    if (message != null && message.getItemId().equals(currentItemKey)) {
                        boolean currentUserInvolved =
                                currentUserId.equals(message.getSenderId()) ||
                                        currentUserId.equals(message.getReceiverId());
                        if (currentUserInvolved) {
                            messageList.add(message);
                            if (message.isUnreadFor(currentUserId) && isChatActive) {
                                child.getRef().child("read").setValue(true);
                            }
                        }
                    }
                }
                messageList.sort((m1, m2) -> Long.compare(m2.getTimestamp(), m1.getTimestamp()));
                chatAdapter.setMessages(messageList);
                if (!messageList.isEmpty()) {
                    chatRecyclerView.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this,
                        "Failed to load messages",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
