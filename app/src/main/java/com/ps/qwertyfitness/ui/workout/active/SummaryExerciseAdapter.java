package com.ps.qwertyfitness.ui.workout.active;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ps.qwertyfitness.R;
import com.ps.qwertyfitness.data.local.entity.WorkoutSet;
import com.ps.qwertyfitness.databinding.ItemSummaryExerciseBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SummaryExerciseAdapter extends RecyclerView.Adapter<SummaryExerciseAdapter.ViewHolder> {

    private List<ActiveExercise> items = new ArrayList<>();

    public void setItems(List<ActiveExercise> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemSummaryExerciseBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSummaryExerciseBinding binding;

        ViewHolder(ItemSummaryExerciseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ActiveExercise exercise) {
            binding.textExerciseName.setText(exercise.name);
            
            int completedSets = 0;
            float totalWeight = 0;
            binding.layoutSetsDetail.removeAllViews();
            
            for (WorkoutSet set : exercise.sets) {
                if (set.isCompleted) {
                    completedSets++;
                    totalWeight += (set.weight * set.reps);
                    
                    TextView setView = new TextView(itemView.getContext());
                    setView.setText(String.format(Locale.getDefault(), "Set %d: %.1f kg x %d", completedSets, set.weight, set.reps));
                    setView.setTextColor(itemView.getContext().getResources().getColor(R.color.text_secondary, null));
                    setView.setTextSize(14);
                    setView.setPadding(0, 4, 0, 4);
                    binding.layoutSetsDetail.addView(setView);
                }
            }
            
            binding.textExerciseSetsSummary.setText(String.format(Locale.getDefault(), "%d sets • %,.0f kg", completedSets, totalWeight));
            binding.layoutSetsDetail.setVisibility(completedSets > 0 ? View.VISIBLE : View.GONE);
        }
    }
}
