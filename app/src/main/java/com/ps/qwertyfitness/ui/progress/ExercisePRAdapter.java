package com.ps.qwertyfitness.ui.progress;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ps.qwertyfitness.data.local.entity.ExercisePR;
import com.ps.qwertyfitness.databinding.ItemExercisePrBinding;

import java.util.List;
import java.util.Locale;

public class ExercisePRAdapter extends ListAdapter<ExercisePR, ExercisePRAdapter.ViewHolder> {
    
    public ExercisePRAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<ExercisePR> DIFF_CALLBACK = new DiffUtil.ItemCallback<ExercisePR>() {
        @Override
        public boolean areItemsTheSame(@NonNull ExercisePR oldItem, @NonNull ExercisePR newItem) {
            return oldItem.exerciseName.equals(newItem.exerciseName);
        }

        @Override
        public boolean areContentsTheSame(@NonNull ExercisePR oldItem, @NonNull ExercisePR newItem) {
            return oldItem.maxWeight == newItem.maxWeight;
        }
    };

    public void setItems(List<ExercisePR> items) {
        submitList(items);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExercisePrBinding binding = ItemExercisePrBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExercisePR item = getItem(position);
        holder.binding.textExerciseName.setText(item.exerciseName);
        holder.binding.textPrWeight.setText(String.format(Locale.getDefault(), "%.1f kg", item.maxWeight));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemExercisePrBinding binding;
        ViewHolder(ItemExercisePrBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}