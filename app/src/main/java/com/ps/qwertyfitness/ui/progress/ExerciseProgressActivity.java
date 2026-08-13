package com.ps.qwertyfitness.ui.progress;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ps.qwertyfitness.R;
import com.ps.qwertyfitness.data.local.entity.ExerciseProgressPoint;
import com.ps.qwertyfitness.data.local.entity.WorkoutSetWithDate;
import com.ps.qwertyfitness.databinding.ActivityExerciseProgressBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExerciseProgressActivity extends AppCompatActivity {

    private ActivityExerciseProgressBinding binding;
    private ProgressViewModel viewModel;
    private SetHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExerciseProgressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ProgressViewModel.class);

        String exerciseName = getIntent().getStringExtra("EXERCISE_NAME");
        binding.toolbar.setTitle(exerciseName);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new SetHistoryAdapter();
        binding.recyclerExerciseHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerExerciseHistory.setAdapter(adapter);

        viewModel.getExerciseDetails(exerciseName).observe(this, exercise -> {
            String nameLower = exerciseName != null ? exerciseName.toLowerCase() : "";
            boolean isDuration = nameLower.contains("plank");
            boolean isBodyweight = (exercise != null && "Bodyweight".equalsIgnoreCase(exercise.equipment)) ||
                                   nameLower.contains("push-up") || 
                                   nameLower.contains("push up") ||
                                   nameLower.contains("sit-up") ||
                                   nameLower.contains("leg raise");
            
            viewModel.getExerciseProgressPoints(exerciseName).observe(this, points -> {
                if (points != null && !points.isEmpty()) {
                    updateHeaderStats(points, isBodyweight, isDuration);
                    
                    List<TrendGraphView.DataPoint> graphPoints = new ArrayList<>();
                    for (ExerciseProgressPoint p : points) {
                        float value;
                        if (isDuration) {
                            value = p.reps; 
                        } else {
                            if (!isBodyweight && p.reps > 0) {
                                value = calculate1RM(p.weight, p.reps);
                            } else {
                                value = (p.weight == 0) ? p.reps : p.weight;
                            }
                        }
                        graphPoints.add(new TrendGraphView.DataPoint(p.timestamp, value));
                    }
                    
                    String label;
                    if (isDuration) {
                        label = "sec";
                        binding.textStatTitle.setText("MAX DURATION");
                    } else if (isBodyweight) {
                        label = "reps";
                        binding.textStatTitle.setText("MAX REPS");
                    } else {
                        label = "kg";
                        binding.textStatTitle.setText("ESTIMATED 1RM");
                    }
                    binding.exerciseGraph.setData(graphPoints, label);
                }
            });
        });

        viewModel.getExerciseHistory(exerciseName).observe(this, sets -> {
            adapter.setItems(sets);
        });
    }

    private void updateHeaderStats(List<ExerciseProgressPoint> points, boolean isBodyweight, boolean isDuration) {
        if (points.isEmpty()) return;

        ExerciseProgressPoint first = points.get(0);
        ExerciseProgressPoint best = points.get(0);
        float bestVal = -1;

        for (ExerciseProgressPoint p : points) {
            float currentVal = isDuration ? p.reps : (isBodyweight ? p.reps : calculate1RM(p.weight, p.reps));
            if (currentVal >= bestVal) {
                bestVal = currentVal;
                best = p;
            }
        }

        if (isDuration) {
            binding.textOneRepMax.setText(formatDuration(best.reps));
            binding.textBestSet.setText(formatDuration(best.reps));
        } else if (isBodyweight) {
            binding.textOneRepMax.setText(String.format(Locale.getDefault(), "%d reps", best.reps));
            binding.textBestSet.setText(String.format(Locale.getDefault(), "%d reps", best.reps));
        } else {
            float oneRM = calculate1RM(best.weight, best.reps);
            binding.textOneRepMax.setText(String.format(Locale.getDefault(), "%.1f kg", oneRM));
            binding.textBestSet.setText(String.format(Locale.getDefault(), "%.1f kg x %d", best.weight, best.reps));
        }

        // Improvement: Compare first set ever to absolute best set
        float firstVal = isDuration ? first.reps : (isBodyweight ? first.reps : calculate1RM(first.weight, first.reps));
        
        if (firstVal > 0) {
            float diff = bestVal - firstVal;
            float percent = (diff / firstVal) * 100;
            binding.textStatImprovement.setText(String.format(Locale.getDefault(), "%s%.0f%%", diff >= 0 ? "+" : "", percent));
            binding.textStatImprovement.setTextColor(diff >= 0 ? 
                    getResources().getColor(R.color.accent_aqua, null) : 
                    getResources().getColor(R.color.error_coral_red, null));
        }
    }

    private float calculate1RM(float weight, int reps) {
        if (reps <= 0) return 0;
        if (reps == 1) return weight;
        // Epley Formula: Weight * (1 + 0.0333 * reps)
        return weight * (1 + (reps / 30f));
    }

    private String formatDuration(int seconds) {
        if (seconds < 60) return seconds + "s";
        int mins = seconds / 60;
        int secs = seconds % 60;
        if (secs == 0) return mins + "m";
        return String.format(Locale.getDefault(), "%dm %ds", mins, secs);
    }
}
