package com.example.amp_mini_project.Firebase;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.amp_mini_project.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DatabaseChatAdapter extends RecyclerView.Adapter<DatabaseChatAdapter.CombinedMessageViewHolder> {

    private List<DatabaseMessage> messageList;
    private final String currentUserId;

    public DatabaseChatAdapter(List<DatabaseMessage> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        sortMessages();
    }

    private void sortMessages() {
        Collections.sort(messageList, new Comparator<DatabaseMessage>() {
            @Override
            public int compare(DatabaseMessage m1, DatabaseMessage m2) {
                return Long.compare(m1.getTimestamp(), m2.getTimestamp());
            }
        });
    }

    @NonNull
    @Override
    public CombinedMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Always inflate the combined layout.
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_bubble, parent, false);
        return new CombinedMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CombinedMessageViewHolder holder, int position) {
        DatabaseMessage message = messageList.get(position);
        holder.bind(message, position);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void setMessages(List<DatabaseMessage> messages) {
        this.messageList = messages;
        sortMessages();
        notifyDataSetChanged();
    }

    class CombinedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, dateTime;
        ImageView readReceiptIcon;
        ImageView profileIconLeft, profileIconRight;
        Button copyEmailButton, copyPhoneButton;
        LinearLayout bubbleContainer;
        LinearLayout rootLayout;

        CombinedMessageViewHolder(View itemView) {
            super(itemView);
            rootLayout = (LinearLayout) itemView;
            messageText = itemView.findViewById(R.id.message_text);
            dateTime = itemView.findViewById(R.id.date_time);
            readReceiptIcon = itemView.findViewById(R.id.read_receipt_icon);
            profileIconLeft = itemView.findViewById(R.id.profile_icon_left);
            profileIconRight = itemView.findViewById(R.id.profile_icon_right);
            copyEmailButton = itemView.findViewById(R.id.copy_email_button);
            copyPhoneButton = itemView.findViewById(R.id.copy_phone_button);
            bubbleContainer = itemView.findViewById(R.id.bubble_container);
        }

        void bind(DatabaseMessage message, int position) {
            messageText.setText(message.getText());
            dateTime.setText(message.getFormattedTimestamp());
            copyEmailButton.setVisibility(message.isSendEmail() ? View.VISIBLE : View.GONE);
            copyPhoneButton.setVisibility(message.isSendPhoneNumber() ? View.VISIBLE : View.GONE);
            boolean isSent = message.getSenderId().equals(currentUserId);
            if (isSent) {
                readReceiptIcon.setVisibility(View.VISIBLE);
                if (message.isRead()) {
                    readReceiptIcon.setImageResource(R.drawable.ic_read);
                } else {
                    readReceiptIcon.setImageResource(R.drawable.ic_unread);
                }
                profileIconLeft.setVisibility(View.GONE);
                if (shouldShowProfile(position)) {
                    profileIconRight.setVisibility(View.VISIBLE);
                    loadSenderProfile(message.getSenderId(), itemView.getContext(), true);
                } else {
                    profileIconRight.setVisibility(View.INVISIBLE);
                }
                rootLayout.setGravity(Gravity.END);
                dateTime.setGravity(Gravity.END);
            } else {
                readReceiptIcon.setVisibility(View.GONE);
                profileIconRight.setVisibility(View.GONE);
                if (shouldShowProfile(position)) {
                    profileIconLeft.setVisibility(View.VISIBLE);
                    loadSenderProfile(message.getSenderId(), itemView.getContext(), false);
                } else {
                    profileIconLeft.setVisibility(View.INVISIBLE);
                }
                rootLayout.setGravity(Gravity.START);
                dateTime.setGravity(Gravity.START);
            }
        }

        private boolean shouldShowProfile(int position) {
            if (position == 0) {
                return true;
            }
            DatabaseMessage previous = messageList.get(position - 1);
            return !previous.getSenderId().equals(messageList.get(position).getSenderId());
        }

        private void loadSenderProfile(String senderId, Context context, boolean isSent) {
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users").child(senderId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    DatabaseUser user = snapshot.getValue(DatabaseUser.class);
                    if (user != null) {
                        String profileImageUrl = user.getProfileUri();
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            if (isSent) {
                                Glide.with(context)
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.ic_placeholder)
                                        .into(profileIconRight);
                            } else {
                                Glide.with(context)
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.ic_placeholder)
                                        .into(profileIconLeft);
                            }
                        }
                        if (copyEmailButton.getVisibility() == View.VISIBLE) {
                            copyEmailButton.setOnClickListener(v -> {
                                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("email", user.getEmail() != null ? user.getEmail() : "");
                                if (clipboard != null) {
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(context, "Email copied", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        if (copyPhoneButton.getVisibility() == View.VISIBLE) {
                            copyPhoneButton.setOnClickListener(v -> {
                                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("phone", user.getPhone() != null ? user.getPhone() : "");
                                if (clipboard != null) {
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(context, "Phone number copied", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }
}
