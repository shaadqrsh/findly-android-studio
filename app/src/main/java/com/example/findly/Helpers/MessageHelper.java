package com.example.findly.Helpers;

import androidx.annotation.NonNull;

import com.example.findly.Firebase.DatabaseMessage;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MessageHelper {
    private static MessageHelper instance;
    private DatabaseReference messagesRef;
    private ChildEventListener childListener;
    private List<MessageUpdateListener> listeners = new ArrayList<>();

    public interface MessageUpdateListener {
        void onMessageUpdated(DatabaseMessage message);
    }

    private MessageHelper() {
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");
    }

    public static synchronized MessageHelper getInstance() {
        if (instance == null) {
            instance = new MessageHelper();
        }
        return instance;
    }

    public void startListening(String currentItemKey) {
        if (childListener == null) {
            childListener = new ChildEventListener() {
                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                    DatabaseMessage updatedMessage = snapshot.getValue(DatabaseMessage.class);
                    if (updatedMessage != null) {
                        updatedMessage.setKey(snapshot.getKey());
                        for (MessageUpdateListener listener : listeners) {
                            listener.onMessageUpdated(updatedMessage);
                        }
                    }
                }
                @Override public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {}
                @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
                @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            };
            messagesRef.orderByChild("itemId").equalTo(currentItemKey)
                    .addChildEventListener(childListener);
        }
    }

    public void stopListening() {
        if (childListener != null) {
            messagesRef.removeEventListener(childListener);
            childListener = null;
        }
    }

    public void addListener(MessageUpdateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(MessageUpdateListener listener) {
        listeners.remove(listener);
    }
}
