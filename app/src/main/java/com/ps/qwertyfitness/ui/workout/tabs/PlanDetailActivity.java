package com.ps.qwertyfitness.ui.workout.tabs;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ps.qwertyfitness.databinding.ActivityPlanDetailBinding;
import com.ps.qwertyfitness.ui.workout.WorkoutViewModel;
import com.ps.qwertyfitness.ui.workout.active.ActiveWorkoutActivity;

public class PlanDetailActivity extends AppCompatActivity {

    private ActivityPlanDetailBinding binding;
    private WorkoutViewModel viewModel;
    private PlanDetailAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlanDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        long planId = getIntent().getLongExtra("PLAN_ID", -1);
        String planName = getIntent().getStringExtra("PLAN_NAME");
        
        binding.toolbar.setTitle(planName);
        
        viewModel = new ViewModelProvider(this).get(WorkoutViewModel.class);
        
        adapter = new PlanDetailAdapter();
        binding.recyclerPlanExercises.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerPlanExercises.setAdapter(adapter);
        
        if (planId != -1) {
            viewModel.getExercisesForPlan(planId).observe(this, exercises -> {
                adapter.setItems(exercises);
            });
        }
        
        binding.btnStartWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(this, ActiveWorkoutActivity.class);
            intent.putExtra("PLAN_ID", planId);
            intent.putExtra("PLAN_NAME", planName);
            startActivity(intent);
            finish();
        });
    }
}