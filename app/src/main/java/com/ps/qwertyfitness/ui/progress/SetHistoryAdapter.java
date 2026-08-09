package com.ps.qwertyfitness.ui.progress;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ps.qwertyfitness.data.local.entity.WorkoutSetWithDate;
import com.ps.qwertyfitness.databinding.ItemWeightHistoryBinding;

import java.util.List;
import java.util.Locale;

public class SetHistoryAdapter extends ListAdapter<WorkoutSetWithDate, SetHistoryAdapter.ViewHolder> {

    public SetHistoryAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<WorkoutSetWithDate> DIFF_CALLBACK = new DiffUtil.ItemCallback<WorkoutSetWithDate>() {
        @Override
        public boolean areItemsTheSame(@NonNull WorkoutSetWithDate oldItem, @NonNull WorkoutSetWithDate newItem) {
            return oldItem.workoutSet.id == newItem.workoutSet.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull WorkoutSetWithDate oldItem, @NonNull WorkoutSetWithDate newItem) {
            return oldItem.workoutSet.weight == newItem.workoutSet.weight && oldItem.date.equals(newItem.date);
        }
    };

    public void setItems(List<WorkoutSetWithDate> items) {
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
        WorkoutSetWithDate item = getItem(position);
        holder.binding.textWeight.setText(String.format(Locale.getDefault(), "%.1f kg x %d", item.workoutSet.weight, item.workoutSet.reps));
        holder.binding.textDate.setText(item.date);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemWeightHistoryBinding binding;
        ViewHolder(ItemWeightHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
