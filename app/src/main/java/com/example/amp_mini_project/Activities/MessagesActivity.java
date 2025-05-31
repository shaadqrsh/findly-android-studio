package com.example.findly.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findly.Firebase.DatabaseMessage;
import com.example.findly.Firebase.DatabaseMessageAdapter;
import com.example.findly.Helpers.MyApp;
import com.example.findly.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagesActivity extends AppCompatActivity {

    private DatabaseMessageAdapter adapter;
    private List<DatabaseMessage> messageList = new ArrayList<>();
    private LinearLayout loadingOverlay;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setVisibility(View.GONE);
        loadingOverlay = findViewById(R.id.loading_overlay);
        loadingOverlay.setVisibility(View.VISIBLE);
        setupBottomNavigation();
        MyApp app = (MyApp) getApplication();
        currentUserId = app.getUserId();
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DatabaseMessageAdapter(this, messageList, currentUserId);
        recyclerView.setAdapter(adapter);
        fetchMessages();
    }

    private void fetchMessages() {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages");

        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, DatabaseMessage> lastMessageMap = new HashMap<>();
                Map<String, Integer> unreadCountMap = new HashMap<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    DatabaseMessage dbMsg = child.getValue(DatabaseMessage.class);
                    if (dbMsg != null) {
                        boolean involved = currentUserId.equals(dbMsg.getSenderId())
                                || currentUserId.equals(dbMsg.getReceiverId());
                        if (!involved) {
                            continue;
                        }
                        String conversationKey = dbMsg.getOtherPersonId(currentUserId) + "_" + dbMsg.getItemId();
                        int unreadSoFar = unreadCountMap.containsKey(conversationKey)
                                ? unreadCountMap.get(conversationKey)
                                : 0;
                        if (dbMsg.isUnreadFor(currentUserId)) {
                            unreadSoFar++;
                        }
                        unreadCountMap.put(conversationKey, unreadSoFar);
                        DatabaseMessage existing = lastMessageMap.get(conversationKey);
                        if (existing == null || dbMsg.getTimestamp() > existing.getTimestamp()) {
                            lastMessageMap.put(conversationKey, dbMsg);
                        }
                    }
                }
                messageList.clear();
                for (String key : lastMessageMap.keySet()) {
                    DatabaseMessage lastMsg = lastMessageMap.get(key);
                    int totalUnread = unreadCountMap.get(key);
                    lastMsg.setConversationUnreadCount(totalUnread);
                    messageList.add(lastMsg);
                }
                messageList.sort((m1, m2) -> Long.compare(m2.getTimestamp(), m1.getTimestamp()));
                adapter.notifyDataSetChanged();
                if(messageList.isEmpty()){
                    Toast.makeText(MessagesActivity.this, "No conversations found", Toast.LENGTH_SHORT).show();
                }
                loadingOverlay.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingOverlay.setVisibility(View.GONE);
            }
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
        textView.setTextColor(getResources().getColor(R.color.myPrimary));
        ImageView imageView = findViewById(R.id.icon_messages);
        imageView.setColorFilter(getResources().getColor(R.color.myPrimary));
    }

    protected void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}