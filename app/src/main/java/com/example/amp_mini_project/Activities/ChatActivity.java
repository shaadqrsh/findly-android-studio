package com.example.amp_mini_project.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amp_mini_project.Firebase.*;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText chatInput;
    private Button sendButton;
    private DatabaseMessageAdapter chatAdapter;
    private List<DatabaseMessage> messageList;

    // Replace this with the actual current user ID (e.g., from your authentication system)
    private String currentUserId = "CURRENT_USER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize views
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        chatInput = findViewById(R.id.chat_input);
        sendButton = findViewById(R.id.send_button);

        // Initialize the message list and adapter
        messageList = new ArrayList<>();
        chatAdapter = new DatabaseMessageAdapter(messageList, currentUserId);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Handle send button click
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String text = chatInput.getText().toString().trim();
                if (!text.isEmpty()) {
                    // For this example, we create a new DatabaseMessage.
                    // You should replace "RECEIVER_ID" and "ITEM_ID" with real values.
                    DatabaseMessage message = new DatabaseMessage(
                            currentUserId,
                            "RECEIVER_ID",
                            "ITEM_ID",
                            text,
                            System.currentTimeMillis(),
                            false,
                            false,
                            false
                    );
                    // Add the message to the list and notify adapter
                    messageList.add(message);
                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                    chatRecyclerView.scrollToPosition(messageList.size() - 1);
                    chatInput.setText("");

                    // Here, you could also send the message to your server or Firebase.
                }
            }
        });

        // Optionally, load existing messages from your database and update the adapter.
    }
}
