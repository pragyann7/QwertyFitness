package com.ps.qwertyfitness.ui.diet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ps.qwertyfitness.R;
import com.ps.qwertyfitness.data.local.entity.UserProfile;
import com.ps.qwertyfitness.databinding.FragmentDietBinding;

import java.util.Locale;

import androidx.recyclerview.widget.LinearLayoutManager;

public class DietFragment extends Fragment {
    
    private FragmentDietBinding binding;
    private DietViewModel viewModel;
    private LoggedFoodAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDietBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DietViewModel.class);

        adapter = new LoggedFoodAdapter(log -> {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Remove Food")
                    .setMessage("Are you sure you want to remove " + log.foodName + "?")
                    .setPositiveButton("Remove", (dialog, which) -> viewModel.deleteLoggedFood(log))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        binding.recyclerMeals.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerMeals.setAdapter(adapter);

        viewModel.getLoggedFoodsToday().observe(getViewLifecycleOwner(), loggedFoods -> {
            adapter.setItems(loggedFoods);
        });

        // Use a more robust way to handle combined data
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                updateNutritionUI(profile, 0f, 0f, 0f, 0f);
                
                viewModel.getTotalCaloriesToday().observe(getViewLifecycleOwner(), consumed -> {
                    viewModel.getTotalProteinToday().observe(getViewLifecycleOwner(), protein -> {
                        viewModel.getTotalCarbsToday().observe(getViewLifecycleOwner(), carbs -> {
                            viewModel.getTotalFatToday().observe(getViewLifecycleOwner(), fat -> {
                                updateNutritionUI(profile, 
                                    consumed != null ? consumed : 0f,
                                    protein != null ? protein : 0f,
                                    carbs != null ? carbs : 0f,
                                    fat != null ? fat : 0f);
                            });
                        });
                    });
                });
            }
        });
        
        binding.btnAddFood.setOnClickListener(v -> {
            AddFoodBottomSheet bottomSheet = new AddFoodBottomSheet();
            bottomSheet.show(getChildFragmentManager(), "AddFoodBottomSheet");
        });
    }

    private void updateNutritionUI(UserProfile profile, float consumed, float protein, float carbs, float fat) {
        binding.textTotalCalories.setText(String.format(Locale.getDefault(), "%,d", profile.dailyCalorieTarget));
        binding.textConsumed.setText(String.format(Locale.getDefault(), "%,.0f", consumed));
        
        int remaining = profile.dailyCalorieTarget - (int) consumed;
        binding.textRemaining.setText(String.format(Locale.getDefault(), "%,d", remaining));
        
        int progress = (int) ((consumed / profile.dailyCalorieTarget) * 100);
        binding.caloriesProgress.setProgress(progress);
        
        binding.textProtein.setText(String.format(Locale.getDefault(), "%.0f / %d g", protein, profile.proteinTarget));
        binding.textCarbs.setText(String.format(Locale.getDefault(), "%.0f / %d g", carbs, profile.carbTarget));
        binding.textFat.setText(String.format(Locale.getDefault(), "%.0f / %d g", fat, profile.fatTarget));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}