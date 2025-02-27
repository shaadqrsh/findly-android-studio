package com.example.amp_mini_project.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amp_mini_project.Firebase.DatabaseMessage;
import com.example.amp_mini_project.Firebase.DatabaseMessagesAdapter;
import com.example.amp_mini_project.Helpers.MyApp;
import com.example.amp_mini_project.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private DatabaseMessagesAdapter adapter;
    private List<DatabaseMessage> messageList = new ArrayList<>();

    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setVisibility(View.GONE);
        setupBottomNavigation();
        MyApp app = (MyApp) getApplication();
        currentUserId = app.getUserId();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DatabaseMessagesAdapter(this, messageList, currentUserId);
        recyclerView.setAdapter(adapter);
        fetchMessages();
    }

    private void fetchMessages() {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages");
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    DatabaseMessage dbMsg = child.getValue(DatabaseMessage.class);
                    if (dbMsg != null) {
                        String senderId = dbMsg.getSenderId();
                        String receiverId = dbMsg.getReceiverId();
                        if (currentUserId.equals(senderId) || currentUserId.equals(receiverId)) {
                            messageList.add(dbMsg);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    protected void setupBottomNavigation() {
        LinearLayout lostButton = findViewById(R.id.button_lost);
        LinearLayout foundButton = findViewById(R.id.button_found);
        LinearLayout mineButton = findViewById(R.id.button_mine);
        LinearLayout profileButton = findViewById(R.id.button_profile);

        lostButton.setOnClickListener(v -> navigateTo(LostListActivity.class));
        foundButton.setOnClickListener(v -> navigateTo(FoundListActivity.class));
        mineButton.setOnClickListener(v -> navigateTo(MineListActivity.class));
        profileButton.setOnClickListener(v -> navigateTo(ProfileActivity.class));

        LinearLayout messagesButton = findViewById(R.id.button_messages);
        messagesButton.setBackgroundColor(getColor(R.color.mySecondary));

        TextView textView = findViewById(R.id.text_messages);
        textView.setTextColor(getResources().getColor(R.color.black));

        ImageView imageView = findViewById(R.id.icon_messages);
        imageView.setColorFilter(getResources().getColor(R.color.black));
    }

    protected void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}