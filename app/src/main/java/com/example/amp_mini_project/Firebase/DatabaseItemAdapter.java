package com.example.findly.Firebase;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.findly.Activities.ItemDetailActivity;
import com.example.findly.Helpers.MyApp;
import com.example.findly.R;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class DatabaseItemAdapter extends RecyclerView.Adapter<DatabaseItemAdapter.ItemViewHolder> {
    private List<DatabaseItem> itemList;
    private Context context;
    private boolean imageLoaded = false;

    public DatabaseItemAdapter(Context context, List<DatabaseItem> itemList, Runnable itemLoadedCallback) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_card, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        DatabaseItem item = itemList.get(position);
        holder.itemName.setText(item.getName());
        holder.itemCategory.setText(item.getCategory());
        holder.itemDate.setText(item.displayDate());
        holder.itemTime.setText(item.displayTime());
        String userId = ((MyApp) context.getApplicationContext()).getUserId();
        if (item.getUploaderId().equals(userId))
        {
            holder.itemUploader.setText(item.getStatus() == 0 ? "Open" : "Closed");
        }
        else {
            item.getUserData("name", "Unknown User", name -> {
                holder.itemUploader.setText(name);
            });
        }
        holder.imageLoadingProgress.setVisibility(View.VISIBLE);
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getImageUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_placeholder)
                            .error(R.drawable.ic_error))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            holder.imageLoadingProgress.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.imageLoadingProgress.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(holder.itemImage);
        } else {
            holder.imageLoadingProgress.setVisibility(View.GONE);
            holder.itemImage.setImageResource(R.drawable.ic_placeholder);
        }
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ItemDetailActivity.class);
            intent.putExtra("itemKey", item.getKey());
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public boolean isImageLoaded() {
        return imageLoaded;
    }

    public void setImageLoaded(boolean imageLoaded) {
        this.imageLoaded = imageLoaded;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemCategory, itemDate, itemTime, itemUploader;
        ImageView itemImage;
        ProgressBar imageLoadingProgress;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            itemCategory = itemView.findViewById(R.id.itemCategory);
            itemDate = itemView.findViewById(R.id.itemDate);
            itemTime = itemView.findViewById(R.id.itemTime);
            itemUploader = itemView.findViewById(R.id.uploaderName);
            itemImage = itemView.findViewById(R.id.itemImage);
            imageLoadingProgress = itemView.findViewById(R.id.imageLoadingProgress);
        }
    }

}
