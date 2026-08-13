package com.ps.qwertyfitness.ui.progress;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ps.qwertyfitness.data.local.entity.WeightEntry;
import com.ps.qwertyfitness.databinding.ItemWeightHistoryBinding;

import java.util.List;
import java.util.Locale;

public class WeightHistoryAdapter extends ListAdapter<WeightEntry, WeightHistoryAdapter.ViewHolder> {
    
    private final OnWeightDeleteListener deleteListener;

    public interface OnWeightDeleteListener {
        void onWeightDelete(WeightEntry entry);
    }

    public WeightHistoryAdapter(OnWeightDeleteListener deleteListener) {
        super(DIFF_CALLBACK);
        this.deleteListener = deleteListener;
    }

    private static final DiffUtil.ItemCallback<WeightEntry> DIFF_CALLBACK = new DiffUtil.ItemCallback<WeightEntry>() {
        @Override
        public boolean areItemsTheSame(@NonNull WeightEntry oldItem, @NonNull WeightEntry newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull WeightEntry oldItem, @NonNull WeightEntry newItem) {
            return oldItem.weight == newItem.weight && oldItem.date.equals(newItem.date);
        }
    };

    public void setItems(List<WeightEntry> items) {
        submitList(items);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWeightHistoryBinding binding = ItemWeightHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeightEntry item = getItem(position);
        holder.binding.textWeight.setText(String.format(Locale.getDefault(), "%.1f kg", item.weight));
        holder.binding.textDate.setText(item.date);
        
        holder.itemView.setOnLongClickListener(v -> {
            deleteListener.onWeightDelete(item);
            return true;
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemWeightHistoryBinding binding;
        ViewHolder(ItemWeightHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}