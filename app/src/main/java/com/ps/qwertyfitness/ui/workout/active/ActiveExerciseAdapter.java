package com.ps.qwertyfitness.ui.workout.active;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ps.qwertyfitness.data.local.entity.WorkoutSet;
import com.ps.qwertyfitness.databinding.ItemActiveExerciseBinding;
import com.ps.qwertyfitness.databinding.ItemActiveSetBinding;

import java.util.ArrayList;
import java.util.List;

public class ActiveExerciseAdapter extends RecyclerView.Adapter<ActiveExerciseAdapter.ViewHolder> {
    
    private List<ActiveExercise> items = new ArrayList<>();
    private final OnSetCompletedListener listener;

    public interface OnSetCompletedListener {
        void onSetCompleted();
    }

    public ActiveExerciseAdapter(OnSetCompletedListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ActiveExercise> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemActiveExerciseBinding binding = ItemActiveExerciseBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActiveExercise exercise = items.get(position);
        holder.binding.textExerciseName.setText(exercise.name);
        holder.binding.textPreviousSession.setText(exercise.previousSession);
        
        boolean isBodyweight = "Bodyweight".equalsIgnoreCase(exercise.equipment);
        holder.binding.textWeightLabel.setText(isBodyweight ? "Extra (kg)" : "Weight");

        holder.binding.layoutSets.removeAllViews();
        for (int i = 0; i < exercise.sets.size(); i++) {
            WorkoutSet set = exercise.sets.get(i);
            ItemActiveSetBinding setBinding = ItemActiveSetBinding.inflate(LayoutInflater.from(holder.itemView.getContext()), holder.binding.layoutSets, false);
            setBinding.textSetNumber.setText(String.valueOf(i + 1));
            setBinding.editWeight.setText(set.weight > 0 ? String.valueOf(set.weight) : "");
            setBinding.editWeight.setHint("0"); 
            
            setBinding.editReps.setText(set.reps > 0 ? String.valueOf(set.reps) : "");
            setBinding.editReps.setHint(exercise.targetReps);
            setBinding.checkCompleted.setChecked(set.isCompleted);
            
            // Set input order: for bodyweight, reps is primary
            if (isBodyweight) {
                setBinding.editReps.requestFocus();
            }
            
            setBinding.editWeight.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    try {
                        set.weight = Float.parseFloat(s.toString());
                    } catch (Exception e) {
                        set.weight = 0;
                    }
                }
            });

            setBinding.editReps.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    try {
                        set.reps = Integer.parseInt(s.toString());
                    } catch (Exception e) {
                        set.reps = 0;
                    }
                }
            });

            setBinding.checkCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                set.isCompleted = isChecked;
                if (isChecked) {
                    listener.onSetCompleted();
                }
            });
            
            holder.binding.layoutSets.addView(setBinding.getRoot());
        }
        
        holder.binding.btnAddSet.setOnClickListener(v -> {
            WorkoutSet newSet = new WorkoutSet();
            newSet.setNumber = exercise.sets.size() + 1;
            exercise.sets.add(newSet);
            notifyItemChanged(holder.getBindingAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemActiveExerciseBinding binding;
        ViewHolder(ItemActiveExerciseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}