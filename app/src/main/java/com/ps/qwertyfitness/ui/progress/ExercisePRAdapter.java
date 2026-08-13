package com.ps.qwertyfitness.ui.progress;

import android.view.LayoutInflater;
import android.view.View;
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
    
    private final OnPrClickListener listener;

    public interface OnPrClickListener {
        void onPrClick(ExercisePR pr);
    }

    public ExercisePRAdapter(OnPrClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<ExercisePR> DIFF_CALLBACK = new DiffUtil.ItemCallback<ExercisePR>() {
        @Override
        public boolean areItemsTheSame(@NonNull ExercisePR oldItem, @NonNull ExercisePR newItem) {
            return oldItem.exerciseName.equals(newItem.exerciseName);
        }

        @Override
        public boolean areContentsTheSame(@NonNull ExercisePR oldItem, @NonNull ExercisePR newItem) {
            return oldItem.maxWeight == newItem.maxWeight && oldItem.maxReps == newItem.maxReps;
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
        
        String nameLower = item.exerciseName.toLowerCase();
        boolean isBodyweight = "Bodyweight".equalsIgnoreCase(item.equipment) ||
                               nameLower.contains("push-up") || 
                               nameLower.contains("push up") ||
                               nameLower.contains("plank") ||
                               nameLower.contains("sit-up") ||
                               nameLower.contains("leg raise");

        if (isBodyweight) {
            String repsText = String.format(Locale.getDefault(), "%d reps", item.maxReps);
            if (item.maxWeight > 0) {
                repsText += String.format(Locale.getDefault(), " (+%.1f kg)", item.maxWeight);
            }
            holder.binding.textPrWeight.setText(repsText);
            
            if (item.initialReps > 0 && item.maxReps > item.initialReps) {
                float diff = item.maxReps - item.initialReps;
                float percent = (diff / item.initialReps) * 100;
                holder.binding.textPrImprovement.setText(String.format(Locale.getDefault(), "+%.0f%%", percent));
                holder.binding.textPrImprovement.setVisibility(View.VISIBLE);
            } else {
                holder.binding.textPrImprovement.setVisibility(View.GONE);
            }
        } else {
            holder.binding.textPrWeight.setText(String.format(Locale.getDefault(), "%.1f kg x %d", item.maxWeight, item.maxReps));
            
            float current1RM = calculate1RM(item.maxWeight, item.maxReps);
            float initial1RM = calculate1RM(item.initialWeight, item.initialReps);
            
            if (initial1RM > 0 && current1RM > initial1RM) {
                float diff = current1RM - initial1RM;
                float percent = (diff / initial1RM) * 100;
                holder.binding.textPrImprovement.setText(String.format(Locale.getDefault(), "+%.0f%%", percent));
                holder.binding.textPrImprovement.setVisibility(View.VISIBLE);
            } else {
                holder.binding.textPrImprovement.setVisibility(View.GONE);
            }
        }
        
        holder.itemView.setOnClickListener(v -> listener.onPrClick(item));
    }

    private float calculate1RM(float weight, int reps) {
        if (reps <= 0) return 0;
        if (reps == 1) return weight;
        // Epley Formula: Weight * (1 + 0.0333 * reps)
        // More robust for higher rep counts
        return weight * (1 + (reps / 30f));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemExercisePrBinding binding;
        ViewHolder(ItemExercisePrBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}