package com.example.mathforkids;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class CloudImageAdapter extends RecyclerView.Adapter<CloudImageAdapter.CloudImageViewHolder> {
    private final List<CloudImage> items;
    private final OnDeleteClickListener onDeleteClickListener;

    public CloudImageAdapter(List<CloudImage> items) {
        this(items, null);
    }

    public CloudImageAdapter(List<CloudImage> items, OnDeleteClickListener onDeleteClickListener) {
        this.items = items;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public CloudImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cloud_image, parent, false);
        return new CloudImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CloudImageViewHolder holder, int position) {
        CloudImage item = items.get(position);
        holder.tvImageInfo.setText("صورة حل للسؤال:\n" + item.getQuestion());
        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .centerCrop()
                .into(holder.imgSolution);
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), ImagePreviewActivity.class);
            intent.putExtra(ImagePreviewActivity.EXTRA_IMAGE_URL, item.getImageUrl());
            intent.putExtra(ImagePreviewActivity.EXTRA_QUESTION, item.getQuestion());
            view.getContext().startActivity(intent);
        });
        holder.btnDeleteImageItem.setOnClickListener(view -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CloudImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSolution;
        TextView tvImageInfo;
        MaterialButton btnDeleteImageItem;

        CloudImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSolution = itemView.findViewById(R.id.imgSolution);
            tvImageInfo = itemView.findViewById(R.id.tvImageInfo);
            btnDeleteImageItem = itemView.findViewById(R.id.btnDeleteImageItem);
        }
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(CloudImage item);
    }
}
