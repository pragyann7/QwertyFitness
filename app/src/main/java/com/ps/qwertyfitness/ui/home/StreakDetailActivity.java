package com.ps.qwertyfitness.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ps.qwertyfitness.R;
import com.ps.qwertyfitness.databinding.ActivityStreakDetailBinding;
import com.ps.qwertyfitness.databinding.ItemCalendarDayBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class StreakDetailActivity extends AppCompatActivity {

    private ActivityStreakDetailBinding binding;
    private HomeViewModel viewModel;
    private Calendar currentMonth;
    private CalendarAdapter adapter;
    private Set<String> workoutDates = new HashSet<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStreakDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        currentMonth = Calendar.getInstance();

        adapter = new CalendarAdapter();
        binding.recyclerCalendar.setLayoutManager(new GridLayoutManager(this, 7));
        binding.recyclerCalendar.setAdapter(adapter);

        binding.btnPrevMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        binding.btnNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        viewModel.getActiveStreak().observe(this, streak -> {
            binding.textDetailStreakCount.setText(String.valueOf(streak));
        });

        viewModel.getWorkoutDates().observe(this, dates -> {
            this.workoutDates = dates;
            updateCalendar();
            updateStats();
            updateStreakStatus();
        });
    }

    private void updateStreakStatus() {
        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        boolean hasWorkoutToday = workoutDates != null && workoutDates.contains(todayStr);

        if (hasWorkoutToday) {
            binding.textStreakStatus.setText("Great job! You've secured your streak for today.");
            binding.textStreakStatus.setTextColor(getResources().getColor(R.color.accent_electric_lime, null));
        } else {
            binding.textStreakStatus.setText("Work out today to keep your streak!");
            binding.textStreakStatus.setTextColor(getResources().getColor(R.color.text_secondary, null));
        }
    }

    private void updateCalendar() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        binding.textMonthYear.setText(sdf.format(currentMonth.getTime()));

        List<Date> days = new ArrayList<>();
        Calendar cal = (Calendar) currentMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        // Find how many days to skip at start (Mon-Sun logic)
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        // Map Sun(1)->6, Mon(2)->0, Tue(3)->1 ... Sat(7)->5
        int skipDays = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : firstDayOfWeek - 2;
        
        cal.add(Calendar.DAY_OF_MONTH, -skipDays);

        // Fill 6 weeks (42 days) to keep grid stable
        for (int dayIndex = 0; dayIndex < 42; dayIndex++) {
            days.add(cal.getTime());
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        adapter.setDays(days);
    }

    private void updateStats() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        String currentMonthStr = sdf.format(currentMonth.getTime());
        
        int monthCount = 0;
        for (String date : workoutDates) {
            if (date.startsWith(currentMonthStr)) {
                monthCount++;
            }
        }
        binding.textMonthTotal.setText(String.format(Locale.getDefault(), "%d Workouts", monthCount));
        
        // Longest Streak logic
        new Thread(() -> {
            int bestStreak = calculateBestStreak();
            runOnUiThread(() -> binding.textBestStreak.setText(String.format(Locale.getDefault(), "%d Days", bestStreak)));
        }).start();
    }

    private int calculateBestStreak() {
        if (workoutDates == null || workoutDates.isEmpty()) return 0;
        
        List<String> sortedDates = new ArrayList<>(workoutDates);
        java.util.Collections.sort(sortedDates);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        int maxStreak = 0;
        int currentStreak = 0;
        String prevDate = null;

        for (String dateStr : sortedDates) {
            if (prevDate == null) {
                currentStreak = 1;
            } else {
                try {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(sdf.parse(prevDate));
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                    String expectedDate = sdf.format(cal.getTime());

                    if (dateStr.equals(expectedDate)) {
                        currentStreak++;
                    } else {
                        maxStreak = Math.max(maxStreak, currentStreak);
                        currentStreak = 1;
                    }
                } catch (Exception e) {
                    currentStreak = 1;
                }
            }
            prevDate = dateStr;
        }
        maxStreak = Math.max(maxStreak, currentStreak);
        
        return maxStreak;
    }

    private class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {
        private List<Date> days = new ArrayList<>();
        private final SimpleDateFormat dateKeySdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        void setDays(List<Date> days) {
            this.days = days;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(ItemCalendarDayBinding.inflate(getLayoutInflater(), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Date date = days.get(position);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            
            holder.binding.textCalendarDay.setText(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
            
            // Fade out days not in current month
            if (cal.get(Calendar.MONTH) != currentMonth.get(Calendar.MONTH)) {
                holder.binding.textCalendarDay.setAlpha(0.2f);
            } else {
                holder.binding.textCalendarDay.setAlpha(1.0f);
            }

            String key = dateKeySdf.format(date);
            boolean hasCurrent = workoutDates.contains(key);
            
            if (hasCurrent) {
                holder.binding.viewStreakBackground.setVisibility(View.VISIBLE);
                holder.binding.textCalendarDay.setTextColor(getResources().getColor(R.color.background_deep_charcoal, null));
                
                // Connection Logic (Duolingo Style)
                boolean hasPrev = false;
                if (position % 7 != 0 && position > 0) {
                    String prevKey = dateKeySdf.format(days.get(position - 1));
                    hasPrev = workoutDates.contains(prevKey);
                }
                
                boolean hasNext = false;
                if (position % 7 != 6 && position < days.size() - 1) {
                    String nextKey = dateKeySdf.format(days.get(position + 1));
                    hasNext = workoutDates.contains(nextKey);
                }

                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.binding.viewStreakBackground.getLayoutParams();
                
                if (hasPrev && hasNext) {
                    holder.binding.viewStreakBackground.setBackgroundResource(R.drawable.shape_streak_middle);
                    params.leftMargin = 0;
                    params.rightMargin = 0;
                } else if (hasPrev) {
                    holder.binding.viewStreakBackground.setBackgroundResource(R.drawable.shape_streak_end);
                    params.leftMargin = 0;
                    params.rightMargin = 12;
                } else if (hasNext) {
                    holder.binding.viewStreakBackground.setBackgroundResource(R.drawable.shape_streak_start);
                    params.leftMargin = 12;
                    params.rightMargin = 0;
                } else {
                    holder.binding.viewStreakBackground.setBackgroundResource(R.drawable.shape_streak_single);
                    params.leftMargin = 12;
                    params.rightMargin = 12;
                }
                holder.binding.viewStreakBackground.setLayoutParams(params);
            } else {
                holder.binding.viewStreakBackground.setVisibility(View.GONE);
                holder.binding.textCalendarDay.setTextColor(getResources().getColor(R.color.text_primary, null));
                
                // Today highlight outline
                String today = dateKeySdf.format(new Date());
                if (key.equals(today)) {
                    holder.binding.textCalendarDay.setBackgroundResource(R.drawable.shape_circle_outline);
                    // Ensure the outline is visible even on dark background
                    android.graphics.drawable.GradientDrawable gd = (android.graphics.drawable.GradientDrawable) holder.binding.textCalendarDay.getBackground();
                    if (gd != null) gd.setStroke(3, getResources().getColor(R.color.accent_electric_lime, null));
                } else {
                    holder.binding.textCalendarDay.setBackground(null);
                }
            }
            
            holder.binding.viewWorkoutIndicator.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return days.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ItemCalendarDayBinding binding;
            ViewHolder(ItemCalendarDayBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
