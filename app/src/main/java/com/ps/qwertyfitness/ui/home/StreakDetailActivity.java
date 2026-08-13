package com.ps.qwertyfitness.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
        });
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
        sortedDates.sort(java.util.Collections.reverseOrder());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        int maxStreak = 0;
        int currentStreak = 0;
        
        if (sortedDates.isEmpty()) return 0;

        try {
            Date firstDate = sdf.parse(sortedDates.get(0));
            if (firstDate == null) return 0;

            Calendar cal = Calendar.getInstance();
            cal.setTime(firstDate);
            
            for (int i = 0; i < sortedDates.size(); i++) {
                if (i > 0) {
                    Calendar expected = (Calendar) cal.clone();
                    expected.add(Calendar.DAY_OF_YEAR, -1);
                    String expectedDate = sdf.format(expected.getTime());
                    
                    if (sortedDates.get(i).equals(expectedDate)) {
                        currentStreak++;
                        cal.setTime(expected.getTime());
                    } else {
                        maxStreak = Math.max(maxStreak, currentStreak);
                        currentStreak = 1;
                        Date nextDate = sdf.parse(sortedDates.get(i));
                        if (nextDate != null) cal.setTime(nextDate);
                    }
                } else {
                    currentStreak = 1;
                }
            }
            maxStreak = Math.max(maxStreak, currentStreak);
        } catch (Exception e) {
            return 0;
        }
        
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
            if (workoutDates.contains(key)) {
                holder.binding.viewWorkoutIndicator.setVisibility(View.VISIBLE);
                holder.binding.textCalendarDay.setTextColor(getResources().getColor(R.color.accent_electric_lime, null));
            } else {
                holder.binding.viewWorkoutIndicator.setVisibility(View.GONE);
                holder.binding.textCalendarDay.setTextColor(getResources().getColor(R.color.text_primary, null));
            }
            
            // Today highlight
            String today = dateKeySdf.format(new Date());
            if (key.equals(today)) {
                holder.binding.textCalendarDay.setBackgroundResource(R.drawable.shape_circle_outline);
            } else {
                holder.binding.textCalendarDay.setBackground(null);
            }
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
