package com.ps.qwertyfitness.ui.workout.active;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.ps.qwertyfitness.data.local.entity.WorkoutSet;
import com.ps.qwertyfitness.databinding.ActivityActiveWorkoutBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActiveWorkoutActivity extends AppCompatActivity {
    
    private ActivityActiveWorkoutBinding binding;
    private ActiveWorkoutViewModel viewModel;
    private ActiveExerciseAdapter adapter;
    private List<ActiveExercise> exercises = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityActiveWorkoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        viewModel = new ViewModelProvider(this).get(ActiveWorkoutViewModel.class);
        
        adapter = new ActiveExerciseAdapter(() -> {
            viewModel.startRestTimer(90000); // 90 seconds
        });
        
        binding.recyclerActiveExercises.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerActiveExercises.setAdapter(adapter);
        
        long planId = getIntent().getLongExtra("PLAN_ID", -1);
        String planName = getIntent().getStringExtra("PLAN_NAME");
        if (planName == null) planName = "Custom Workout";
        
        binding.toolbar.setTitle(planName);
        viewModel.startWorkout(planName);

        if (planId != -1) {
            viewModel.getExercisesForPlan(planId).observe(this, planExercises -> {
                if (planExercises != null && exercises.isEmpty()) { // Only load once
                    for (com.ps.qwertyfitness.data.local.entity.PlanExerciseWithDetails pe : planExercises) {
                        ActiveExercise ae = new ActiveExercise(pe.exercise.name, pe.exercise.id, pe.planExercise.repsRange);
                        for (int i = 0; i < pe.planExercise.sets; i++) {
                            ae.sets.add(new WorkoutSet());
                        }
                        exercises.add(ae);
                    }
                    viewModel.loadPreviousData(exercises, () -> {
                        runOnUiThread(() -> adapter.setItems(exercises));
                    });
                }
            });
        }
        
        viewModel.getIsTimerRunning().observe(this, isRunning -> {
            binding.layoutTimer.setVisibility(isRunning ? View.VISIBLE : View.GONE);
        });
        
        viewModel.getRestTimeRemaining().observe(this, remaining -> {
            int seconds = (int) (remaining / 1000);
            int minutes = seconds / 60;
            seconds %= 60;
            binding.textRestTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        });
        
        binding.btnSkipTimer.setOnClickListener(v -> viewModel.skipTimer());
        
        binding.btnFinishWorkout.setOnClickListener(v -> {
            viewModel.finishWorkout(exercises);
            finish();
        });
    }
}
