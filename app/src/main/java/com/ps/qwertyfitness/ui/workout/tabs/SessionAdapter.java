package com.ps.qwertyfitness.ui.workout.tabs;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ps.qwertyfitness.data.local.entity.WorkoutSession;
import com.ps.qwertyfitness.databinding.ItemWorkoutSessionBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.ViewHolder> {
    
    private List<WorkoutSession> items = new ArrayList<>();

    public void setItems(List<WorkoutSession> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWorkoutSessionBinding binding = ItemWorkoutSessionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutSession item = items.get(position);
        holder.binding.textSessionName.setText(item.planName);
        holder.binding.textSessionDate.setText(item.date);
        holder.binding.textSessionVolume.setText(String.format(Locale.getDefault(), "%,d kg", item.totalVolume));
        holder.binding.textSessionSets.setText(String.format(Locale.getDefault(), "%d sets", item.totalSets));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemWorkoutSessionBinding binding;
        ViewHolder(ItemWorkoutSessionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}