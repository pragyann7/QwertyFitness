package com.ps.qwertyfitness.ui.workout.tabs;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ps.qwertyfitness.data.local.entity.WorkoutPlan;
import com.ps.qwertyfitness.databinding.ItemWorkoutPlanBinding;

import java.util.ArrayList;
import java.util.List;

public class PlansAdapter extends RecyclerView.Adapter<PlansAdapter.ViewHolder> {
    
    private List<WorkoutPlan> items = new ArrayList<>();
    private final OnPlanClickListener listener;

    public interface OnPlanClickListener {
        void onPlanClick(WorkoutPlan plan);
    }

    public PlansAdapter(OnPlanClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<WorkoutPlan> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWorkoutPlanBinding binding = ItemWorkoutPlanBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutPlan item = items.get(position);
        holder.binding.textPlanName.setText(item.name);
        holder.binding.textPlanDetails.setText(String.format("%d days/week • %s", item.trainingDaysPerWeek, item.difficulty));
        holder.itemView.setOnClickListener(v -> listener.onPlanClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemWorkoutPlanBinding binding;
        ViewHolder(ItemWorkoutPlanBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}