package com.ps.qwertyfitness.ui.progress;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ps.qwertyfitness.data.local.entity.WeightEntry;
import com.ps.qwertyfitness.databinding.ItemWeightHistoryBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WeightHistoryAdapter extends RecyclerView.Adapter<WeightHistoryAdapter.ViewHolder> {
    
    private List<WeightEntry> items = new ArrayList<>();

    public void setItems(List<WeightEntry> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWeightHistoryBinding binding = ItemWeightHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeightEntry item = items.get(position);
        holder.binding.textWeight.setText(String.format(Locale.getDefault(), "%.1f kg", item.weight));
        holder.binding.textDate.setText(item.date);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemWeightHistoryBinding binding;
        ViewHolder(ItemWeightHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}