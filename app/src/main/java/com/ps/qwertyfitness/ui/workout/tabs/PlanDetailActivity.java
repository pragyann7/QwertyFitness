package com.ps.qwertyfitness.ui.workout.tabs;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
    private long planId;
    private String planName;
    private boolean isRecommended;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlanDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        planId = getIntent().getLongExtra("PLAN_ID", -1);
        planName = getIntent().getStringExtra("PLAN_NAME");
        isRecommended = getIntent().getBooleanExtra("IS_RECOMMENDED", false);
        
        binding.toolbar.setTitle(planName);
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
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

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        if (!isRecommended) {
            getMenuInflater().inflate(com.ps.qwertyfitness.R.menu.menu_plan_detail, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == com.ps.qwertyfitness.R.id.action_edit) {
            Intent intent = new Intent(this, CreatePlanActivity.class);
            intent.putExtra("PLAN_ID", planId);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
