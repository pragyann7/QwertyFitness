package com.ps.qwertyfitness.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.ps.qwertyfitness.R;

import androidx.lifecycle.ViewModelProvider;

import com.ps.qwertyfitness.databinding.FragmentHomeBinding;
import com.ps.qwertyfitness.databinding.ItemScheduleBinding;

import java.util.Locale;

import android.content.Intent;
import com.ps.qwertyfitness.ui.workout.active.ActiveWorkoutActivity;
import com.ps.qwertyfitness.data.local.entity.LoggedFood;
import java.util.List;

public class HomeFragment extends Fragment {
    
    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        
        homeViewModel.getUserProfile().observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile != null) {
                binding.textGreeting.setText(getString(R.string.greeting_format, userProfile.name));
                binding.textCaloriesTarget.setText(getString(R.string.kcal_format, String.format(Locale.getDefault(), "%,d", userProfile.dailyCalorieTarget)));
                
                homeViewModel.getTotalCaloriesToday().observe(getViewLifecycleOwner(), consumed -> {
                    float consumedVal = consumed != null ? consumed : 0f;
                    binding.textCaloriesConsumed.setText(getString(R.string.consumed_format, String.format(Locale.getDefault(), "%,.0f", consumedVal)));
                    binding.textCaloriesRemaining.setText(getString(R.string.remaining_format, String.format(Locale.getDefault(), "%,d", (int)(userProfile.dailyCalorieTarget - consumedVal))));
                });

                homeViewModel.getTotalProteinToday().observe(getViewLifecycleOwner(), protein -> {
                    float proteinVal = protein != null ? protein : 0f;
                    binding.progressProtein.setProgress((int) ((proteinVal / userProfile.proteinTarget) * 100));
                });

                homeViewModel.getTotalCarbsToday().observe(getViewLifecycleOwner(), carbs -> {
                    float carbsVal = carbs != null ? carbs : 0f;
                    binding.progressCarbs.setProgress((int) ((carbsVal / userProfile.carbTarget) * 100));
                });

                homeViewModel.getTotalFatToday().observe(getViewLifecycleOwner(), fat -> {
                    float fatVal = fat != null ? fat : 0f;
                    binding.progressFat.setProgress((int) ((fatVal / userProfile.fatTarget) * 100));
                });

                homeViewModel.getLoggedFoodsToday().observe(getViewLifecycleOwner(), loggedFoods -> {
                    updateSchedule(loggedFoods);
                });
            }
        });

        binding.btnStartWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ActiveWorkoutActivity.class);
            intent.putExtra("PLAN_NAME", "Push Day");
            startActivity(intent);
        });
    }

    private void updateSchedule(List<LoggedFood> loggedFoods) {
        binding.layoutSchedule.removeAllViews();
        
        addMealToSchedule("08:00", "Breakfast", "650 kcal", isLogged(loggedFoods, "Breakfast"));
        addMealToSchedule("13:00", "Lunch", "700 kcal", isLogged(loggedFoods, "Lunch"));
        addMealToSchedule("18:00", "Push Workout", "Upcoming", false);
        addMealToSchedule("20:00", "Dinner", "750 kcal", isLogged(loggedFoods, "Dinner"));
    }

    private boolean isLogged(List<LoggedFood> logs, String type) {
        if (logs == null) return false;
        for (LoggedFood log : logs) {
            if (type.equalsIgnoreCase(log.mealType)) return true;
        }
        return false;
    }

    private void addMealToSchedule(String time, String title, String subtitle, boolean isDone) {
        ItemScheduleBinding itemBinding = ItemScheduleBinding.inflate(getLayoutInflater(), binding.layoutSchedule, false);
        itemBinding.textTime.setText(time);
        itemBinding.textTitle.setText(title);
        itemBinding.textSubtitle.setText(subtitle);
        itemBinding.textStatus.setText(isDone ? "✓" : "○");
        itemBinding.textStatus.setTextColor(isDone ? 
                getResources().getColor(R.color.accent_electric_lime, null) : 
                getResources().getColor(R.color.text_muted, null));
        
        binding.layoutSchedule.addView(itemBinding.getRoot());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
