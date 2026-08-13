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
        
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmationDialog();
            }
        });

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
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> showExitConfirmationDialog());
        }
        
        viewModel.startWorkout(planName);

        if (planId != -1) {
            viewModel.getExercisesForPlan(planId).observe(this, planExercises -> {
                if (planExercises != null && exercises.isEmpty()) { // Only load once
                    for (com.ps.qwertyfitness.data.local.entity.PlanExerciseWithDetails pe : planExercises) {
                        ActiveExercise ae = new ActiveExercise(pe.exercise.name, pe.exercise.id, pe.planExercise.repsRange, pe.exercise.equipment);
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
        
        binding.btn30s.setOnClickListener(v -> viewModel.startRestTimer(30000));
        binding.btn60s.setOnClickListener(v -> viewModel.startRestTimer(60000));
        binding.btn90s.setOnClickListener(v -> viewModel.startRestTimer(90000));
        binding.btn180s.setOnClickListener(v -> viewModel.startRestTimer(180000));

        binding.btnAddExercise.setOnClickListener(v -> {
            com.ps.qwertyfitness.ui.workout.tabs.ExercisePickerBottomSheet bottomSheet = new com.ps.qwertyfitness.ui.workout.tabs.ExercisePickerBottomSheet();
            bottomSheet.setListener(exercises -> {
                for (com.ps.qwertyfitness.data.local.entity.Exercise exercise : exercises) {
                    ActiveExercise ae = new ActiveExercise(exercise.name, exercise.id, "8-12", exercise.equipment);
                    ae.sets.add(new com.ps.qwertyfitness.data.local.entity.WorkoutSet());
                    ActiveWorkoutActivity.this.exercises.add(ae);
                }
                adapter.setItems(ActiveWorkoutActivity.this.exercises);
            });
            bottomSheet.show(getSupportFragmentManager(), "ExercisePicker");
        });
        
        binding.btnFinishWorkout.setOnClickListener(v -> {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Finish Workout?")
                    .setMessage("Are you sure you want to complete this session?")
                    .setPositiveButton("Finish", (dialog, which) -> {
                        // Stats will be calculated in ViewModel now
                        viewModel.finishWorkout(exercises, prsBroken -> {
                            // Get totals from currentSession after calculation
                            com.ps.qwertyfitness.data.local.entity.WorkoutSession session = viewModel.getCurrentSession();
                            
                            android.content.Intent intent = new android.content.Intent(this, WorkoutSummaryActivity.class);
                            intent.putExtra("PLAN_NAME", binding.toolbar.getTitle());
                            intent.putExtra("DURATION", System.currentTimeMillis() - viewModel.getStartTime());
                            intent.putExtra("VOLUME", session.totalVolume);
                            intent.putExtra("SETS", session.totalSets);
                            intent.putExtra("PRS", prsBroken);
                            intent.putExtra("SESSION_ID", viewModel.getCurrentSessionId());
                            intent.putExtra("EXERCISES", (java.io.Serializable) exercises);
                            startActivity(intent);
                            finish();
                        });
                    })
                    .setNegativeButton("Resume", null)
                    .show();
        });
    }

    private void showExitConfirmationDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Discard Workout?")
                .setMessage("Are you sure you want to leave? Your current progress in this session will not be saved.")
                .setPositiveButton("Discard", (dialog, which) -> finish())
                .setNegativeButton("Keep Training", null)
                .show();
    }
}
