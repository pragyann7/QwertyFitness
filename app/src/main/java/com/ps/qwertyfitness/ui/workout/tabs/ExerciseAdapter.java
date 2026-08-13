package com.ps.qwertyfitness.ui.workout.tabs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ps.qwertyfitness.data.local.entity.Exercise;
import com.ps.qwertyfitness.data.local.entity.ExercisePR;
import com.ps.qwertyfitness.databinding.ItemExerciseBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolder> {
    
    private List<Exercise> items = new ArrayList<>();
    private final Map<String, ExercisePR> prMap = new HashMap<>();
    private OnExerciseClickListener listener;
    
    private boolean multiSelectMode = false;
    private final Set<Long> selectedExerciseIds = new HashSet<>();
    private final Map<Long, Exercise> selectedExercisesMap = new HashMap<>();

    public interface OnExerciseClickListener {
        void onExerciseClick(Exercise exercise);
        void onSelectionChanged(int count);
    }

    public ExerciseAdapter() {}

    public ExerciseAdapter(OnExerciseClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Exercise> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setMultiSelectMode(boolean enabled) {
        this.multiSelectMode = enabled;
        if (!enabled) {
            selectedExerciseIds.clear();
            selectedExercisesMap.clear();
        }
        notifyDataSetChanged();
    }

    public Set<Long> getSelectedExerciseIds() {
        return selectedExerciseIds;
    }
    
    public List<Exercise> getSelectedExercises() {
        return new ArrayList<>(selectedExercisesMap.values());
    }

    public void setPersonalRecords(List<ExercisePR> prs) {
        prMap.clear();
        if (prs != null) {
            for (ExercisePR pr : prs) {
                prMap.put(pr.exerciseName, pr);
            }
        }
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
        Exercise item = items.get(position);
        holder.binding.textExerciseName.setText(item.name);
        holder.binding.textExerciseDetails.setText(item.targetMuscleGroup + " • " + item.equipment);
        
        ExercisePR pr = prMap.get(item.name);
        String nameLower = item.name.toLowerCase();
        boolean isBodyweight = "Bodyweight".equalsIgnoreCase(item.equipment) ||
                nameLower.contains("push-up") ||
                nameLower.contains("push up") ||
                nameLower.contains("plank") ||
                nameLower.contains("sit-up") ||
                nameLower.contains("leg raise");

        if (pr != null) {
            holder.binding.layoutPrBadge.setVisibility(View.VISIBLE);
            if (isBodyweight) {
                String prText = String.format(Locale.getDefault(), "%d reps", pr.maxReps);
                if (pr.maxWeight > 0) {
                    prText += String.format(Locale.getDefault(), " (+%.1fkg)", pr.maxWeight);
                }
                holder.binding.textExercisePr.setText(prText);
            } else {
                holder.binding.textExercisePr.setText(String.format(Locale.getDefault(), "%.1fkg x %d", pr.maxWeight, pr.maxReps));
            }
        } else {
            holder.binding.layoutPrBadge.setVisibility(View.INVISIBLE);
        }

        holder.binding.checkExercise.setVisibility(multiSelectMode ? View.VISIBLE : View.GONE);
        holder.binding.checkExercise.setChecked(selectedExerciseIds.contains(item.id));

        holder.itemView.setOnClickListener(v -> {
            if (multiSelectMode) {
                toggleSelection(item);
            } else if (listener != null) {
                listener.onExerciseClick(item);
            }
        });
        
        holder.binding.checkExercise.setOnClickListener(v -> toggleSelection(item));
    }
    
    private void toggleSelection(Exercise exercise) {
        if (selectedExerciseIds.contains(exercise.id)) {
            selectedExerciseIds.remove(exercise.id);
            selectedExercisesMap.remove(exercise.id);
        } else {
            selectedExerciseIds.add(exercise.id);
            selectedExercisesMap.put(exercise.id, exercise);
        }
        notifyDataSetChanged();
        if (listener != null) listener.onSelectionChanged(selectedExerciseIds.size());
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
