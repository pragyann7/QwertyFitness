package com.ps.qwertyfitness.ui.progress;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ps.qwertyfitness.data.local.entity.ExercisePR;
import com.ps.qwertyfitness.databinding.ItemExercisePrBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExercisePRAdapter extends RecyclerView.Adapter<ExercisePRAdapter.ViewHolder> {
    
    private List<ExercisePR> items = new ArrayList<>();

    public void setItems(List<ExercisePR> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExercisePrBinding binding = ItemExercisePrBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExercisePR item = items.get(position);
        holder.binding.textExerciseName.setText(item.exerciseName);
        holder.binding.textPrWeight.setText(String.format(Locale.getDefault(), "%.1f kg", item.maxWeight));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemExercisePrBinding binding;
        ViewHolder(ItemExercisePrBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}