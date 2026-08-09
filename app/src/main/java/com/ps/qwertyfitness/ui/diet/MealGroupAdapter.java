package com.ps.qwertyfitness.ui.diet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ps.qwertyfitness.R;
import com.ps.qwertyfitness.data.local.entity.LoggedFood;
import com.ps.qwertyfitness.data.local.entity.Reminder;
import com.ps.qwertyfitness.databinding.ItemMealGroupBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MealGroupAdapter extends RecyclerView.Adapter<MealGroupAdapter.ViewHolder> {

    private List<Reminder> mealPlans = new ArrayList<>();
    private List<LoggedFood> loggedFoods = new ArrayList<>();
    private final OnMealAddListener listener;

    public interface OnMealAddListener {
        void onAddFood(String mealType);
    }

    public MealGroupAdapter(OnMealAddListener listener) {
        this.listener = listener;
    }

    public void setMealPlans(List<Reminder> items) {
        this.mealPlans = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setLoggedFoods(List<LoggedFood> foods) {
        this.loggedFoods = foods != null ? foods : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemMealGroupBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(mealPlans.get(position), loggedFoods, listener);
    }

    @Override
    public int getItemCount() {
        return mealPlans.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemMealGroupBinding binding;

        ViewHolder(ItemMealGroupBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Reminder meal, List<LoggedFood> allFoods, OnMealAddListener listener) {
            binding.textMealTitle.setText(meal.title);
            
            float consumed = 0;
            binding.layoutMealItems.removeAllViews();
            
            for (LoggedFood food : allFoods) {
                if (meal.title.equalsIgnoreCase(food.mealType)) {
                    consumed += food.calories;
                    
                    TextView itemText = new TextView(itemView.getContext());
                    itemText.setText(String.format(Locale.getDefault(), "• %s (%.0f kcal)", food.foodName, food.calories));
                    itemText.setTextColor(itemView.getContext().getResources().getColor(R.color.text_secondary, null));
                    itemText.setTextSize(14);
                    itemText.setPadding(0, 4, 0, 4);
                    binding.layoutMealItems.addView(itemText);
                }
            }
            
            binding.textMealCalories.setText(String.format(Locale.getDefault(), "%.0f / %d kcal", consumed, meal.targetValue));
            
            int progress = (int) ((consumed / (float) meal.targetValue) * 100);
            binding.progressMealCalories.setProgress(progress);
            
            binding.btnAddToMeal.setOnClickListener(v -> listener.onAddFood(meal.title));
        }
    }
}
