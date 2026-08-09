package com.ps.qwertyfitness.ui.workout.tabs;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ps.qwertyfitness.databinding.ItemAddedExerciseBinding;

import java.util.ArrayList;
import java.util.List;

public class AddedExerciseAdapter extends RecyclerView.Adapter<AddedExerciseAdapter.ViewHolder> {
    
    private final List<PlanExerciseDraft> items = new ArrayList<>();

    public void addExercise(PlanExerciseDraft draft) {
        items.add(draft);
        notifyItemInserted(items.size() - 1);
    }

    public List<PlanExerciseDraft> getItems() {
        return items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAddedExerciseBinding binding = ItemAddedExerciseBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlanExerciseDraft item = items.get(position);
        holder.binding.textExerciseName.setText(item.exerciseName);
        
        holder.binding.editTargetSets.setText(String.valueOf(item.sets));
        holder.binding.editTargetReps.setText(item.repsRange);

        holder.binding.editTargetSets.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                try {
                    item.sets = Integer.parseInt(s.toString());
                } catch (Exception e) {
                    item.sets = 0;
                }
            }
        });

        holder.binding.editTargetReps.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                item.repsRange = s.toString();
            }
        });

        holder.binding.btnRemove.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                items.remove(pos);
                notifyItemRemoved(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemAddedExerciseBinding binding;
        ViewHolder(ItemAddedExerciseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}