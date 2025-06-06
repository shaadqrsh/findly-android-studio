package com.example.findly.Firebase;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.findly.Activities.ChatActivity;
import com.example.findly.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class DatabaseMessageAdapter extends RecyclerView.Adapter<DatabaseMessageAdapter.MessagesViewHolder> {

    private final Context context;
    private final List<DatabaseMessage> messageList;
    private final String currentUserId;

    public DatabaseMessageAdapter(Context context,
                                  List<DatabaseMessage> messageList,
                                  String currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_message_card, parent, false);
        return new MessagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesViewHolder holder, int position) {
        DatabaseMessage current = messageList.get(position);
        holder.tvLastMessage.setText(current.getText());
        holder.tvTimestamp.setText(current.getFormattedTimestamp());
        int unreadCount = current.getConversationUnreadCount();
        if (unreadCount > 0) {
            holder.tvUnreadCount.setVisibility(View.VISIBLE);
            holder.tvUnreadCount.setText(String.valueOf(unreadCount));
        } else {
            holder.tvUnreadCount.setVisibility(View.GONE);
        }

        String itemId = current.getItemId();
        if (itemId != null && !itemId.isEmpty()) {
            DatabaseReference itemRef = FirebaseDatabase.getInstance()
                    .getReference("entries")
                    .child(itemId);

            itemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    DatabaseItem dbItem = snapshot.getValue(DatabaseItem.class);
                    if (dbItem != null) {
                        holder.tvItemName.setText(dbItem.getName());
                        if (dbItem.getImageUrl() != null && !dbItem.getImageUrl().isEmpty()) {
                            Glide.with(context)
                                    .load(dbItem.getImageUrl())
                                    .into(holder.ivItemImage);
                        } else {
                            holder.ivItemImage.setImageResource(R.drawable.ic_placeholder);
                        }
                    } else {
                        holder.tvItemName.setText("Unknown Item");
                        holder.ivItemImage.setImageResource(R.drawable.ic_placeholder);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    holder.tvItemName.setText("Error Loading Item");
                }
            });
        } else {
            holder.tvItemName.setText("No Item");
            holder.ivItemImage.setImageResource(R.drawable.ic_placeholder);
        }
        String otherPersonId = current.getOtherPersonId(currentUserId);
        if (otherPersonId != null && !otherPersonId.isEmpty()) {
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(otherPersonId)
                    .child("name");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String displayName = snapshot.getValue(String.class);
                    if (displayName != null && !displayName.isEmpty()) {
                        holder.tvOtherPersonName.setText(displayName);
                    } else {
                        holder.tvOtherPersonName.setText("User " + otherPersonId);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    holder.tvOtherPersonName.setText("Error Loading User");
                }
            });
        } else {
            holder.tvOtherPersonName.setText("Unknown User");
        }
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("item_id", itemId);
            intent.putExtra("other_user", otherPersonId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessagesViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItemImage;
        TextView tvItemName, tvOtherPersonName, tvLastMessage, tvUnreadCount, tvTimestamp;

        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItemImage       = itemView.findViewById(R.id.ivItemImage);
            tvItemName        = itemView.findViewById(R.id.tvItemName);
            tvOtherPersonName = itemView.findViewById(R.id.tvOtherPersonName);
            tvLastMessage     = itemView.findViewById(R.id.tvLastMessage);
            tvUnreadCount     = itemView.findViewById(R.id.tvUnreadCount);
            tvTimestamp       = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
