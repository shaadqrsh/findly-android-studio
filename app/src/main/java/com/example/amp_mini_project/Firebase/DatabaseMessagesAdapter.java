package com.example.amp_mini_project.Firebase;

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
import com.example.amp_mini_project.Activities.ChatActivity;
import com.example.amp_mini_project.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class DatabaseMessagesAdapter extends RecyclerView.Adapter<DatabaseMessagesAdapter.MessagesViewHolder> {

    private final Context context;
    private final List<DatabaseMessage> messageList;
    private final String currentUserId;

    public DatabaseMessagesAdapter(Context context,
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
        if (current.isUnreadFor(currentUserId)) {
            holder.tvUnreadCount.setVisibility(View.VISIBLE);
            holder.tvUnreadCount.setText("1");
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
                            holder.ivItemImage.setImageResource(R.drawable.my_placeholder);
                        }
                    } else {
                        holder.tvItemName.setText("Unknown Item");
                        holder.ivItemImage.setImageResource(R.drawable.my_placeholder);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    holder.tvItemName.setText("Error Loading Item");
                }
            });
        } else {
            holder.tvItemName.setText("No Item");
            holder.ivItemImage.setImageResource(R.drawable.my_placeholder);
        }
        String otherPersonId = current.getOtherPersonId(currentUserId);
        if (otherPersonId != null && !otherPersonId.isEmpty()) {
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(otherPersonId)
                    .child("displayName");

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
