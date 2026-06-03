package com.example.mathforkids;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ProgressAdapter extends RecyclerView.Adapter<ProgressAdapter.ProgressViewHolder> {
    private final List<ProgressItem> items;
    private final OnDeleteClickListener onDeleteClickListener;

    public ProgressAdapter(List<ProgressItem> items) {
        this(items, null);
    }

    public ProgressAdapter(List<ProgressItem> items, OnDeleteClickListener onDeleteClickListener) {
        this.items = items;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public ProgressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_progress, parent, false);
        return new ProgressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgressViewHolder holder, int position) {
        ProgressItem item = items.get(position);
        String title = item.isCorrect() ? "إجابة صحيحة" : "نحتاج محاولة أخرى";
        String details = item.getQuestion() + " | إجابتك: " + item.getChildAnswer()
                + " | الصحيح: " + item.getCorrectAnswer();
        holder.tvTitle.setText(title);
        holder.tvDetails.setText(details);
        holder.btnDeleteProgressItem.setOnClickListener(view -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ProgressViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDetails;
        MaterialButton btnDeleteProgressItem;

        ProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            btnDeleteProgressItem = itemView.findViewById(R.id.btnDeleteProgressItem);
        }
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(ProgressItem item);
    }
}
