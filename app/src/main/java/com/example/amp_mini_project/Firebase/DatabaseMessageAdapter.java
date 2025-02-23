package com.example.amp_mini_project.Firebase;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseMessageAdapter extends RecyclerView.Adapter<DatabaseMessageAdapter.CombinedMessageViewHolder> {

    private List<DatabaseMessage> messageList;
    private String currentUserId;

    public DatabaseMessageAdapter(List<DatabaseMessage> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public CombinedMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bubble, parent, false);
        return new CombinedMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CombinedMessageViewHolder holder, int position) {
        DatabaseMessage message = messageList.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class CombinedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, dateTime;
        ImageView readReceiptIcon;
        ImageView profileIconLeft, profileIconRight;
        Button copyEmailButton, copyPhoneButton;
        LinearLayout bubbleContainer;

        CombinedMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            dateTime = itemView.findViewById(R.id.date_time);
            readReceiptIcon = itemView.findViewById(R.id.read_receipt_icon);
            profileIconLeft = itemView.findViewById(R.id.profile_icon_left);
            profileIconRight = itemView.findViewById(R.id.profile_icon_right);
            copyEmailButton = itemView.findViewById(R.id.copy_email_button);
            copyPhoneButton = itemView.findViewById(R.id.copy_phone_button);
            bubbleContainer = itemView.findViewById(R.id.bubble_container);
        }

        void bind(DatabaseMessage message) {
            messageText.setText(message.getText());
            dateTime.setText(formatTimestamp(message.getTimestamp()));
            if (message.isRead()) {
                readReceiptIcon.setImageResource(R.drawable.ic_read);
            } else {
                readReceiptIcon.setImageResource(R.drawable.ic_unread);
            }
            copyEmailButton.setVisibility(message.isSendEmail() ? View.VISIBLE : View.GONE);
            copyPhoneButton.setVisibility(message.isSendPhoneNumber() ? View.VISIBLE : View.GONE);
            boolean isSent = message.getSenderId().equals(currentUserId);
            if (isSent) {
                profileIconLeft.setVisibility(View.GONE);
                profileIconRight.setVisibility(View.VISIBLE);
                // Optionally, adjust bubble container background or alignment for sent messages.
                // bubbleContainer.setBackgroundResource(R.drawable.bubble_background_sent);
            } else {
                profileIconRight.setVisibility(View.GONE);
                profileIconLeft.setVisibility(View.VISIBLE);
                //bubbleContainer.setBackgroundResource(R.drawable.bubble_background_received);
            }
            // Load sender's full DatabaseUser object and update the visible profile icon.
            loadSenderProfile(message.getSenderId(), itemView.getContext(), isSent);
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
                                        .placeholder(R.drawable.my_placeholder)
                                        .into(profileIconRight);
                            } else {
                                Glide.with(context)
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.my_placeholder)
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
                public void onCancelled(@NonNull DatabaseError error) {
                    // Optionally handle error.
                }
            });
        }

        private String formatTimestamp(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
}
