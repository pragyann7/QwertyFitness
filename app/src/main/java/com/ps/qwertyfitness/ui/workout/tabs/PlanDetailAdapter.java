package com.ps.qwertyfitness.ui.workout.tabs;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ps.qwertyfitness.data.local.entity.PlanExerciseWithDetails;
import com.ps.qwertyfitness.databinding.ItemExerciseBinding;

import java.util.ArrayList;
import java.util.List;

public class PlanDetailAdapter extends RecyclerView.Adapter<PlanDetailAdapter.ViewHolder> {
    
    private List<PlanExerciseWithDetails> items = new ArrayList<>();

    public void setItems(List<PlanExerciseWithDetails> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExerciseBinding binding = ItemExerciseBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlanExerciseWithDetails item = items.get(position);
        holder.binding.textExerciseName.setText(item.exercise.name);
        holder.binding.textExerciseDetails.setText(String.format("%d sets • %s reps", 
                item.planExercise.sets, item.planExercise.repsRange));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemExerciseBinding binding;
        ViewHolder(ItemExerciseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}