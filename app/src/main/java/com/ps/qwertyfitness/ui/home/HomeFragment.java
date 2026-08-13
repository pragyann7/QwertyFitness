package com.ps.qwertyfitness.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.ps.qwertyfitness.data.local.entity.UserProfile;
import com.ps.qwertyfitness.data.local.entity.WorkoutPlan;
import com.ps.qwertyfitness.data.repository.FitnessRepository;
import java.util.List;

public class HomeFragment extends Fragment {
    
    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private UserProfile currentUserProfile;
    private int currentTotalWater = 0;
    private List<LoggedFood> currentLoggedFoods;
    private List<Reminder> currentReminders;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void updateGreeting(String name) {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour >= 5 && hour < 12) {
            greeting = getString(R.string.greeting_morning, name);
        } else if (hour >= 12 && hour < 17) {
            greeting = getString(R.string.greeting_afternoon, name);
        } else {
            greeting = getString(R.string.greeting_evening, name);
        }
        binding.textGreeting.setText(greeting);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        
        // Update current date
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEEE, MMMM d", java.util.Locale.getDefault());
        binding.textDate.setText(sdf.format(new java.util.Date()));
        
        setupObservers();

        binding.btnAddWater.setOnClickListener(v -> {
            homeViewModel.logWater(250);
        });

        binding.btnUndoWater.setOnClickListener(v -> {
            homeViewModel.undoWaterLog();
        });

        binding.btnManageSchedule.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), com.ps.qwertyfitness.ui.schedule.ScheduleActivity.class));
        });

        binding.cardStreak.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), StreakDetailActivity.class));
        });

        binding.cardWeeklyActivity.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), StreakDetailActivity.class));
        });
    }

    private void setupObservers() {
        homeViewModel.getUserProfile().observe(getViewLifecycleOwner(), userProfile -> {
            currentUserProfile = userProfile;
            if (userProfile != null) {
                updateGreeting(userProfile.name);
                binding.textCaloriesTarget.setText(getString(R.string.kcal_format, String.format(Locale.getDefault(), "%,d", userProfile.dailyCalorieTarget)));
                updateWaterUI();
                updateCaloriesUI(homeViewModel.getTotalCaloriesToday().getValue());
            }
        });

        homeViewModel.getTotalCaloriesToday().observe(getViewLifecycleOwner(), consumed -> {
            updateCaloriesUI(consumed);
        });

        homeViewModel.getTotalProteinToday().observe(getViewLifecycleOwner(), protein -> {
            if (currentUserProfile != null) {
                float proteinVal = protein != null ? protein : 0f;
                binding.progressProtein.setProgress((int) ((proteinVal / currentUserProfile.proteinTarget) * 100));
            }
        });

        homeViewModel.getTotalCarbsToday().observe(getViewLifecycleOwner(), carbs -> {
            if (currentUserProfile != null) {
                float carbsVal = carbs != null ? carbs : 0f;
                binding.progressCarbs.setProgress((int) ((carbsVal / currentUserProfile.carbTarget) * 100));
            }
        });

        homeViewModel.getTotalFatToday().observe(getViewLifecycleOwner(), fat -> {
            if (currentUserProfile != null) {
                float fatVal = fat != null ? fat : 0f;
                binding.progressFat.setProgress((int) ((fatVal / currentUserProfile.fatTarget) * 100));
            }
        });

        homeViewModel.getLoggedFoodsToday().observe(getViewLifecycleOwner(), loggedFoods -> {
            currentLoggedFoods = loggedFoods;
            updateSchedule(currentReminders, loggedFoods);
        });

        homeViewModel.getAllReminders().observe(getViewLifecycleOwner(), reminders -> {
            currentReminders = reminders;
            updateSchedule(reminders, currentLoggedFoods);
        });

        homeViewModel.getAllPlans().observe(getViewLifecycleOwner(), plans -> {
            updateTodayWorkout(plans);
        });

        homeViewModel.getActiveStreak().observe(getViewLifecycleOwner(), streak -> {
            binding.textStreakCount.setText(String.valueOf(streak));
            if (streak > 0) {
                binding.textWeeklyActivityTitle.setText(getString(R.string.weekly_activity_streak_format, streak));
            } else {
                binding.textWeeklyActivityTitle.setText(getString(R.string.weekly_activity));
            }
        });

        homeViewModel.getWorkoutDates().observe(getViewLifecycleOwner(), dates -> {
            updateWeeklyActivity(dates);
        });

        // Water Tracker
        homeViewModel.getTotalWaterToday().observe(getViewLifecycleOwner(), consumed -> {
            currentTotalWater = consumed != null ? consumed : 0;
            updateWaterUI();
            updateSchedule(currentReminders, currentLoggedFoods);
        });
    }

    private void updateCaloriesUI(Float consumed) {
        if (currentUserProfile != null) {
            float consumedVal = consumed != null ? consumed : 0f;
            binding.textCaloriesConsumed.setText(getString(R.string.consumed_format, String.format(Locale.getDefault(), "%,.0f", consumedVal)));
            binding.textCaloriesRemaining.setText(getString(R.string.remaining_format, String.format(Locale.getDefault(), "%,d", (int) (currentUserProfile.dailyCalorieTarget - consumedVal))));
        }
    }

    private void updateWaterUI() {
        if (currentUserProfile != null) {
            binding.textWaterAmount.setText(getString(R.string.ml_format, currentTotalWater));
            binding.textWaterTarget.setText(getString(R.string.target_ml_format, currentUserProfile.waterTarget));

            int progressValue = (int) (((float) currentTotalWater / currentUserProfile.waterTarget) * 100);
            if (progressValue > 100) progressValue = 100;

            // Smooth progress animation
            android.animation.ObjectAnimator.ofInt(binding.progressWaterCircular, "progress", binding.progressWaterCircular.getProgress(), progressValue)
                    .setDuration(800)
                    .start();
        }
    }

    private void updateWeeklyActivity(java.util.Set<String> workoutDates) {
        binding.layoutWeeklyActivity.removeAllViews();
        
        java.util.Calendar cal = java.util.Calendar.getInstance();
        // Set to Monday of current week
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        java.text.SimpleDateFormat daySdf = new java.text.SimpleDateFormat("E", java.util.Locale.getDefault());
        
        String todayStr = sdf.format(new java.util.Date());

        for (int i = 0; i < 7; i++) {
            String dateStr = sdf.format(cal.getTime());
            String fullDayName = daySdf.format(cal.getTime());
            String dayInitial = !fullDayName.isEmpty() ? fullDayName.substring(0, 1).toUpperCase() : "";
            
            boolean isCompleted = workoutDates != null && workoutDates.contains(dateStr);
            boolean isToday = dateStr.equals(todayStr);

            View dayView = getLayoutInflater().inflate(R.layout.item_activity_day, binding.layoutWeeklyActivity, false);
            TextView textDayName = dayView.findViewById(R.id.text_day_name);
            View indicatorBg = dayView.findViewById(R.id.view_indicator_bg);
            View imgCheck = dayView.findViewById(R.id.img_check);

            textDayName.setText(dayInitial);
            if (isToday) {
                textDayName.setTextColor(getResources().getColor(R.color.accent_electric_lime, null));
                textDayName.setAlpha(1.0f);
            } else {
                textDayName.setAlpha(0.5f);
            }

            if (isCompleted) {
                indicatorBg.setBackgroundResource(R.drawable.shape_circle_filled);
                imgCheck.setVisibility(View.VISIBLE);
            } else {
                indicatorBg.setBackgroundResource(R.drawable.shape_circle_outline);
                imgCheck.setVisibility(View.GONE);
                if (isToday) {
                    // Highlight today's circle outline if not completed yet
                    android.graphics.drawable.GradientDrawable outline = (android.graphics.drawable.GradientDrawable) indicatorBg.getBackground();
                    outline.setStroke(4, getResources().getColor(R.color.accent_electric_lime, null));
                }
            }

            binding.layoutWeeklyActivity.addView(dayView);
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void updateTodayWorkout(List<WorkoutPlan> plans) {
        if (plans == null || plans.isEmpty()) return;

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK); 
        int normalizedDay = (dayOfWeek == java.util.Calendar.SUNDAY) ? 7 : dayOfWeek - 1;
        
        String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String currentDayName = dayNames[dayOfWeek - 1];

        WorkoutPlan todayPlan = null;
        for (WorkoutPlan plan : plans) {
            if (isPlanScheduledForDay(plan, normalizedDay, currentDayName)) {
                todayPlan = plan;
                break;
            }
        }

        if (todayPlan != null) {
            final WorkoutPlan finalPlan = todayPlan;
            binding.textWorkoutName.setText(todayPlan.name);
            
            // Set details based on plan name
            if (todayPlan.name.contains("Push")) binding.textWorkoutDetails.setText("Chest • Shoulders • Triceps");
            else if (todayPlan.name.contains("Pull")) binding.textWorkoutDetails.setText("Back • Rear Delts • Biceps");
            else if (todayPlan.name.contains("Legs")) binding.textWorkoutDetails.setText("Quads • Hamstrings • Glutes • Calves");
            else if (todayPlan.name.contains("Upper")) binding.textWorkoutDetails.setText("Chest • Back • Shoulders • Arms");
            else if (todayPlan.name.contains("Lower")) binding.textWorkoutDetails.setText("Quads • Hamstrings • Glutes • Calves • Core");

            binding.btnStartWorkout.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ActiveWorkoutActivity.class);
                intent.putExtra("PLAN_ID", finalPlan.id);
                intent.putExtra("PLAN_NAME", finalPlan.name);
                startActivity(intent);
            });
        } else {
            binding.textWorkoutName.setText("Rest Day");
            binding.textWorkoutDetails.setText("Take it easy today!");
            binding.btnStartWorkout.setEnabled(false);
            binding.btnStartWorkout.setAlpha(0.5f);
        }
    }

    private boolean isPlanScheduledForDay(WorkoutPlan plan, int normalizedDay, String dayName) {
        if (plan.selectedDays != null && !plan.selectedDays.isEmpty()) {
            String[] days = plan.selectedDays.split(",");
            for (String d : days) {
                if (d.trim().equals(String.valueOf(normalizedDay))) return true;
            }
            return false;
        }
        // Fallback for older plans or recommended ones that might use name-based scheduling
        return plan.name.toLowerCase().contains(dayName.toLowerCase());
    }

    private void updateSchedule(List<Reminder> reminders, List<LoggedFood> loggedFoods) {
        binding.layoutSchedule.removeAllViews();
        
        long now = System.currentTimeMillis();
        java.util.Calendar todayCal = java.util.Calendar.getInstance();
        boolean hasItems = false;

        if (reminders != null) {
            // Sort reminders by time
            java.util.List<Reminder> sortedReminders = new java.util.ArrayList<>(reminders);
            java.util.Collections.sort(sortedReminders, (r1, r2) -> r1.time.compareTo(r2.time));
            
            int waterRemindersCount = 0;

            for (Reminder reminder : sortedReminders) {
                if (!reminder.enabled) continue;
                if (!com.ps.qwertyfitness.utils.ReminderManager.isValidDay(reminder, todayCal)) continue;
                
                hasItems = true;
                boolean isDone;
                String subtitle;
                String statusText;
                int statusIcon = 0;

                if ("WATER".equals(reminder.type)) {
                    waterRemindersCount++;
                    // Automatically mark as done if sufficient water logged (each reminder = 250ml)
                    isDone = (currentTotalWater / 250) >= waterRemindersCount;
                    
                    if (reminder.intervalMinutes > 0) {
                        long nextTime = com.ps.qwertyfitness.utils.ReminderManager.calculateNextTriggerTime(reminder);
                        String timeStr = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(new java.util.Date(nextTime));
                        
                        if (nextTime > getEndOfDay()) {
                            subtitle = "Completed for today";
                            statusText = "✓";
                        } else {
                            subtitle = "Interval: " + reminder.intervalMinutes + "m";
                            statusText = timeStr;
                            statusIcon = R.drawable.ic_refresh;
                        }
                    } else {
                        subtitle = isDone ? "Hydrated" : "Upcoming";
                        statusText = isDone ? "✓" : "○";
                    }
                } else {
                    isDone = isLogged(loggedFoods, reminder.title);
                    if (reminder.snoozeUntil > now && !isDone) {
                        String snoozeTime = new java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(new java.util.Date(reminder.snoozeUntil));
                        subtitle = "LATER (" + snoozeTime + ")";
                        statusText = "○";
                    } else {
                        subtitle = isDone ? "Completed" : "Upcoming";
                        statusText = isDone ? "✓" : "○";
                    }
                }

                addMealToSchedule(reminder.time, reminder.title, subtitle, isDone, reminder.type, statusText, statusIcon, reminder.id);
            }
        }

        if (!hasItems) {
            addMealToSchedule("--:--", "Free Day", "Nothing scheduled for today", false, "INFO", "○", 0, -1);
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
        
        if (!"INFO".equals(type) && !"WATER".equals(type)) {
            itemBinding.getRoot().setOnClickListener(v -> {
                if (!isDone) {
                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Mark as Done")
                            .setMessage("Did you complete this: " + title + "?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                homeViewModel.logActivityCompletion(title);
                                
                                // Reset snooze since it's done now
                                if (reminderId != -1) {
                                    new Thread(() -> {
                                        // Better: just call updateSnooze directly if available in VM
                                        com.ps.qwertyfitness.utils.ReminderManager.cancelSnooze(requireContext(), reminderId);
                                    }).start();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                } else {
                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Unmark")
                            .setMessage("Do you want to unmark this: " + title + "?")
                            .setPositiveButton("Unmark", (dialog, which) -> {
                                homeViewModel.deleteLoggedActivity(title);
                            })
                            .setNegativeButton("Cancel", null)
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
