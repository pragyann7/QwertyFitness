package com.ps.qwertyfitness.ui.workout.tabs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ps.qwertyfitness.R;
import com.ps.qwertyfitness.data.local.entity.WorkoutPlan;
import com.ps.qwertyfitness.databinding.ItemWorkoutPlanBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlansAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<Object> displayItems = new ArrayList<>();
    private final OnPlanClickListener listener;
    
    private boolean isEditMode = false;
    private final Set<Long> selectedPlanIds = new HashSet<>();

    public interface OnPlanClickListener {
        void onPlanClick(WorkoutPlan plan);
        void onEditModeChanged(boolean editMode);
        void onSelectionChanged(int count);
    }

    public PlansAdapter(OnPlanClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<WorkoutPlan> plans) {
        displayItems.clear();
        List<WorkoutPlan> custom = new ArrayList<>();
        List<WorkoutPlan> recommended = new ArrayList<>();

        if (plans != null) {
            for (WorkoutPlan p : plans) {
                if (p.isRecommended) recommended.add(p);
                else custom.add(p);
            }
        }

        if (!custom.isEmpty()) {
            displayItems.add("YOUR PLANS");
            displayItems.addAll(custom);
        }
        
        if (!recommended.isEmpty()) {
            displayItems.add("RECOMMENDED");
            displayItems.addAll(recommended);
        }
        notifyDataSetChanged();
    }

    public void setEditMode(boolean editMode) {
        this.isEditMode = editMode;
        if (!editMode) selectedPlanIds.clear();
        notifyDataSetChanged();
    }

    public Set<Long> getSelectedPlanIds() {
        return selectedPlanIds;
    }

    @Override
    public int getItemViewType(int position) {
        return (displayItems.get(position) instanceof String) ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plan_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            ItemWorkoutPlanBinding binding = ItemWorkoutPlanBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ItemViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).title.setText((String) displayItems.get(position));
        } else if (holder instanceof ItemViewHolder) {
            WorkoutPlan plan = (WorkoutPlan) displayItems.get(position);
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            
            itemHolder.binding.textPlanName.setText(plan.name);
            itemHolder.binding.textPlanDetails.setText(formatPlanDetails(plan));
            
            itemHolder.binding.checkPlan.setVisibility(isEditMode && !plan.isRecommended ? View.VISIBLE : View.GONE);
            itemHolder.binding.checkPlan.setChecked(selectedPlanIds.contains(plan.id));

            itemHolder.itemView.setOnClickListener(v -> {
                if (isEditMode && !plan.isRecommended) {
                    toggleSelection(plan.id);
                } else {
                    listener.onPlanClick(plan);
                }
            });

            itemHolder.itemView.setOnLongClickListener(v -> {
                if (!isEditMode && !plan.isRecommended) {
                    isEditMode = true;
                    toggleSelection(plan.id);
                    listener.onEditModeChanged(true);
                    return true;
                }
                return false;
            });
            
            itemHolder.binding.checkPlan.setOnClickListener(v -> toggleSelection(plan.id));
        }
    }

    private void toggleSelection(long id) {
        if (selectedPlanIds.contains(id)) selectedPlanIds.remove(id);
        else selectedPlanIds.add(id);
        notifyDataSetChanged();
        listener.onSelectionChanged(selectedPlanIds.size());
    }

    private String formatPlanDetails(WorkoutPlan plan) {
        StringBuilder sb = new StringBuilder();
        if (plan.selectedDays != null && !plan.selectedDays.isEmpty()) {
            String[] days = plan.selectedDays.split(",");
            String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            for (int k = 0; k < days.length; k++) {
                try {
                    int dayIdx = Integer.parseInt(days[k].trim()) - 1;
                    if (dayIdx >= 0 && dayIdx < 7) {
                        sb.append(dayNames[dayIdx]);
                        if (k < days.length - 1) sb.append(", ");
                    }
                } catch (Exception e) {
                    // Ignore parsing errors
                }
            }
            if (sb.length() > 0) sb.append(" • ");
        } else {
            sb.append(plan.trainingDaysPerWeek).append(" days/week • ");
        }
        sb.append(plan.difficulty);
        if (plan.reminderTime != null && !plan.reminderTime.isEmpty()) {
            sb.append(" • ").append(plan.reminderTime);
        }
        return sb.toString();
    }

    @Override
    public int getItemCount() {
        return displayItems.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        HeaderViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.text_header_title);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        final ItemWorkoutPlanBinding binding;
        ItemViewHolder(ItemWorkoutPlanBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
