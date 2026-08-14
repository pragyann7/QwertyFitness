package com.ps.qwertyfitness.ui.diet;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ps.qwertyfitness.data.local.entity.LoggedFood;
import com.ps.qwertyfitness.databinding.ItemLoggedFoodBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LoggedFoodAdapter extends RecyclerView.Adapter<LoggedFoodAdapter.ViewHolder> {
    
    private List<LoggedFood> items = new ArrayList<>();
    private final OnFoodDeleteListener listener;

    public interface OnFoodDeleteListener {
        void onFoodDelete(LoggedFood log);
    }

    public LoggedFoodAdapter(OnFoodDeleteListener listener) {
        this.listener = listener;
    }

    public void setItems(List<LoggedFood> items) {
        if (items == null) {
            this.items = new ArrayList<>();
        } else {
            this.items = items;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLoggedFoodBinding binding = ItemLoggedFoodBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LoggedFood item = items.get(position);
        holder.binding.textFoodName.setText(item.foodName);
        
        String unitStr = item.unit != null ? item.unit : "g";
        holder.binding.textFoodInfo.setText(String.format(Locale.getDefault(), "%s • %.0f %s • %.0f kcal", 
                item.mealType, item.quantity, unitStr, item.calories));
        
        holder.itemView.setOnLongClickListener(v -> {
            listener.onFoodDelete(item);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemLoggedFoodBinding binding;
        ViewHolder(ItemLoggedFoodBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}