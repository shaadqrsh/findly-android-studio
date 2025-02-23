package com.example.amp_mini_project.Firebase;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<DatabaseMessage> messageList;
    private String currentUserId;

    public DatabaseMessageAdapter(List<DatabaseMessage> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        DatabaseMessage message = messageList.get(position);
        // Check if the message is sent or received
        if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_bubble_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_bubble_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DatabaseMessage message = messageList.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).bind(message);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ViewHolder for sent messages
    class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, dateTime;
        ImageView readReceiptIcon, profileIcon;
        Button copyEmailButton, copyPhoneButton;

        SentMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            dateTime = itemView.findViewById(R.id.date_time);
            readReceiptIcon = itemView.findViewById(R.id.read_receipt_icon);
            profileIcon = itemView.findViewById(R.id.profile_icon);
            copyEmailButton = itemView.findViewById(R.id.copy_email_button);
            copyPhoneButton = itemView.findViewById(R.id.copy_phone_button);
        }

        void bind(DatabaseMessage message) {
            messageText.setText(message.getText());
            dateTime.setText(formatTimestamp(message.getTimestamp()));
            // You might want to update the read receipt icon based on message.isRead()
            // and set click listeners for the copy buttons.
        }
    }

    // ViewHolder for received messages
    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, dateTime;
        ImageView readReceiptIcon, profileIcon;
        Button copyEmailButton, copyPhoneButton;

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            dateTime = itemView.findViewById(R.id.date_time);
            readReceiptIcon = itemView.findViewById(R.id.read_receipt_icon);
            profileIcon = itemView.findViewById(R.id.profile_icon);
            copyEmailButton = itemView.findViewById(R.id.copy_email_button);
            copyPhoneButton = itemView.findViewById(R.id.copy_phone_button);
        }

        void bind(DatabaseMessage message) {
            messageText.setText(message.getText());
            dateTime.setText(formatTimestamp(message.getTimestamp()));
            // Configure additional UI elements (copy buttons, profile image, etc.) as needed.
        }
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
