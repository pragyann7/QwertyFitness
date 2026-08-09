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
import com.ps.qwertyfitness.data.local.entity.Reminder;
import com.ps.qwertyfitness.data.repository.FitnessRepository;
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
                    homeViewModel.getAllReminders().observe(getViewLifecycleOwner(), reminders -> {
                        updateSchedule(reminders, loggedFoods);
                    });
                });
            }
        });

        binding.btnStartWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ActiveWorkoutActivity.class);
            intent.putExtra("PLAN_NAME", "Push Day");
            startActivity(intent);
        });

        binding.btnManageSchedule.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), com.ps.qwertyfitness.ui.schedule.ScheduleActivity.class));
        });
    }

    private void updateSchedule(List<Reminder> reminders, List<LoggedFood> loggedFoods) {
        binding.layoutSchedule.removeAllViews();
        
        if (reminders == null || reminders.isEmpty()) {
            addMealToSchedule("08:00", "Breakfast", "No reminders set", false, "MEAL", "○", 0, -1);
        } else {
            long now = System.currentTimeMillis();
            for (Reminder reminder : reminders) {
                if (!reminder.enabled) continue;
                
                boolean isDone = isLogged(loggedFoods, reminder.title);
                String subtitle;
                String statusText;
                int statusIcon = 0;

                if (reminder.snoozeUntil > now && !isDone) {
                    String snoozeTime = new java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(new java.util.Date(reminder.snoozeUntil));
                    subtitle = "LATER (" + snoozeTime + ")";
                    statusText = "○";
                } else if ("WATER".equals(reminder.type) && reminder.intervalMinutes > 0) {
                    long nextTime = com.ps.qwertyfitness.utils.ReminderManager.calculateNextTriggerTime(reminder);
                    String timeStr = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(new java.util.Date(nextTime));
                    
                    if (nextTime > getEndOfDay()) {
                        subtitle = "Tomorrow: " + timeStr;
                    } else {
                        subtitle = "Interval: " + reminder.intervalMinutes + "m";
                    }
                    statusText = timeStr;
                    statusIcon = R.drawable.ic_refresh;
                } else {
                    subtitle = isDone ? "Completed" : "Upcoming";
                    statusText = isDone ? "✓" : "○";
                }

                addMealToSchedule(reminder.time, reminder.title, subtitle, isDone, reminder.type, statusText, statusIcon, reminder.id);
            }
        }
    }

    private boolean isLogged(List<LoggedFood> logs, String type) {
        if (logs == null) return false;
        for (LoggedFood log : logs) {
            if (type.equalsIgnoreCase(log.mealType)) return true;
        }
        return false;
    }

    private long getEndOfDay() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.set(java.util.Calendar.HOUR_OF_DAY, 23);
        c.set(java.util.Calendar.MINUTE, 59);
        c.set(java.util.Calendar.SECOND, 59);
        return c.getTimeInMillis();
    }

    private void addMealToSchedule(String time, String title, String subtitle, boolean isDone, String type, String statusText, int statusIcon, long reminderId) {
        ItemScheduleBinding itemBinding = ItemScheduleBinding.inflate(getLayoutInflater(), binding.layoutSchedule, false);
        itemBinding.textTime.setText(time);
        itemBinding.textTitle.setText(title);
        itemBinding.textSubtitle.setText(subtitle);
        itemBinding.textStatus.setText(statusText);
        
        if (statusIcon != 0) {
            itemBinding.imgStatusIcon.setImageResource(statusIcon);
            itemBinding.imgStatusIcon.setVisibility(View.VISIBLE);
        } else {
            itemBinding.imgStatusIcon.setVisibility(View.GONE);
        }

        itemBinding.textStatus.setTextColor(isDone ? 
                getResources().getColor(R.color.accent_electric_lime, null) : 
                getResources().getColor(R.color.text_muted, null));
        
        if (!"WATER".equals(type)) {
            itemBinding.getRoot().setOnClickListener(v -> {
                if (!isDone) {
                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Mark as Done")
                            .setMessage("Did you complete this: " + title + "?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                FitnessRepository repo = new FitnessRepository(requireActivity().getApplication());
                                // Log item to mark as done
                                LoggedFood food = new LoggedFood();
                                food.foodName = title;
                                food.mealType = title; 
                                food.date = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
                                food.calories = 0;
                                repo.logFood(food);
                                
                                // Reset snooze since it's done now
                                if (reminderId != -1) {
                                    new Thread(() -> {
                                        repo.updateSnoozeTime(reminderId, 0);
                                        com.ps.qwertyfitness.utils.ReminderManager.cancelSnooze(requireContext(), reminderId);
                                    }).start();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            });
        }
        
        binding.layoutSchedule.addView(itemBinding.getRoot());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
