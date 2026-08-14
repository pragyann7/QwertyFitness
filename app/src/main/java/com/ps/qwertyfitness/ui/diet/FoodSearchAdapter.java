package com.ps.qwertyfitness.ui.diet;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ps.qwertyfitness.R;
import com.ps.qwertyfitness.data.local.entity.FoodItem;
import com.ps.qwertyfitness.databinding.ItemFoodSearchBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodSearchAdapter extends RecyclerView.Adapter<FoodSearchAdapter.ViewHolder> {
    
    private List<FoodItem> items = new ArrayList<>();
    private final OnFoodClickListener listener;
    private int selectedPosition = -1;

    public interface OnFoodClickListener {
        void onFoodClick(FoodItem food);
    }

    public FoodSearchAdapter(OnFoodClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<FoodItem> items) {
        this.selectedPosition = -1;
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
        ItemFoodSearchBinding binding = ItemFoodSearchBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem item = items.get(position);
        holder.binding.textFoodName.setText(item.name);
        
        String unitStr = item.unit != null ? item.unit : "100g";
        holder.binding.textFoodInfo.setText(String.format(Locale.getDefault(), "%.0f kcal per %s", item.calories, unitStr));
        
        if (item.category != null) {
            holder.binding.textFoodCategory.setText(item.category.toUpperCase());
            holder.binding.textFoodCategory.setVisibility(android.view.View.VISIBLE);
        } else {
            holder.binding.textFoodCategory.setVisibility(android.view.View.GONE);
        }

        // Highlight selection with premium style
        if (selectedPosition == position) {
            holder.binding.cardRoot.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.surface_elevated_soft_slate));
            holder.binding.cardRoot.setStrokeWidth(2);
            holder.binding.cardRoot.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.accent_electric_lime));
            holder.binding.textFoodName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.accent_electric_lime));
        } else {
            holder.binding.cardRoot.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.transparent));
            holder.binding.cardRoot.setStrokeWidth(0);
            holder.binding.textFoodName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
        }

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getBindingAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            listener.onFoodClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemFoodSearchBinding binding;
        ViewHolder(ItemFoodSearchBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}