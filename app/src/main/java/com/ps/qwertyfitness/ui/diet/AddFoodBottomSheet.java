package com.ps.qwertyfitness.ui.diet;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ps.qwertyfitness.data.local.entity.FoodItem;
import com.ps.qwertyfitness.data.local.entity.LoggedFood;
import com.ps.qwertyfitness.databinding.LayoutAddFoodBottomSheetBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddFoodBottomSheet extends BottomSheetDialogFragment {
    
    private LayoutAddFoodBottomSheetBinding binding;
    private DietViewModel viewModel;
    private FoodItem selectedFood;
    private FoodSearchAdapter adapter;
    private String preselectedMealType = null;
    private final String[] mealTypes = {"Breakfast", "Lunch", "Dinner", "Snack"};

    public static AddFoodBottomSheet newInstance(String mealType) {
        AddFoodBottomSheet fragment = new AddFoodBottomSheet();
        fragment.preselectedMealType = mealType;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutAddFoodBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireParentFragment()).get(DietViewModel.class);
        
        ArrayAdapter<String> mealAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, mealTypes);
        binding.dropdownMealType.setAdapter(mealAdapter);
        
        if (preselectedMealType != null) {
            binding.dropdownMealType.setText(preselectedMealType, false);
        } else {
            binding.dropdownMealType.setText(mealTypes[3], false); // Default to Snack
        }

        adapter = new FoodSearchAdapter(food -> {
            selectedFood = food;
            binding.layoutQuantity.setVisibility(View.VISIBLE);
            binding.editQuantity.requestFocus();
        });
        
        binding.recyclerFoodSearch.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerFoodSearch.setAdapter(adapter);
        
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), foodItems -> {
            adapter.setItems(foodItems);
        });
        
        binding.editSearchFood.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnAdd.setOnClickListener(v -> {
            if (selectedFood != null) {
                String qtyStr = binding.editQuantity.getText().toString();
                if (!qtyStr.isEmpty()) {
                    float qty = Float.parseFloat(qtyStr);
                    LoggedFood log = new LoggedFood();
                    log.foodItemId = selectedFood.id;
                    log.foodName = selectedFood.name;
                    log.quantity = qty;
                    log.calories = (selectedFood.calories * qty) / 100;
                    log.protein = (selectedFood.protein * qty) / 100;
                    log.carbs = (selectedFood.carbs * qty) / 100;
                    log.fat = (selectedFood.fat * qty) / 100;
                    log.date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                    log.mealType = binding.dropdownMealType.getText().toString();
                    
                    viewModel.logFood(log);
                    dismiss();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}