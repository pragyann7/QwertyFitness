package com.ps.qwertyfitness.ui.workout.tabs;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ps.qwertyfitness.data.local.entity.PlanExercise;
import com.ps.qwertyfitness.data.local.entity.WorkoutPlan;
import com.ps.qwertyfitness.data.repository.FitnessRepository;
import com.ps.qwertyfitness.databinding.ActivityCreatePlanBinding;

import java.util.ArrayList;
import java.util.List;

public class CreatePlanActivity extends AppCompatActivity {

    private ActivityCreatePlanBinding binding;
    private AddedExerciseAdapter adapter;
    private FitnessRepository repository;
    private final String[] difficultyLevels = {"Beginner", "Intermediate", "Advanced"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreatePlanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        repository = new FitnessRepository(getApplication());
        
        ArrayAdapter<String> diffAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, difficultyLevels);
        binding.dropdownDifficulty.setAdapter(diffAdapter);
        binding.dropdownDifficulty.setText(difficultyLevels[1], false); // Default to Intermediate

        adapter = new AddedExerciseAdapter();
        binding.recyclerPlanExercises.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerPlanExercises.setAdapter(adapter);
        
        binding.btnAddExercise.setOnClickListener(v -> {
            ExercisePickerBottomSheet picker = new ExercisePickerBottomSheet();
            picker.setListener(exercise -> adapter.addExercise(new PlanExerciseDraft(exercise.id, exercise.name)));
            picker.show(getSupportFragmentManager(), "ExercisePicker");
        });
        
        binding.btnSavePlan.setOnClickListener(v -> {
            String name = binding.editPlanName.getText().toString();
            String daysStr = binding.editDaysPerWeek.getText().toString();
            
            if (!name.isEmpty() && !daysStr.isEmpty()) {
                WorkoutPlan plan = new WorkoutPlan();
                plan.name = name;
                plan.trainingDaysPerWeek = Integer.parseInt(daysStr);
                plan.difficulty = binding.dropdownDifficulty.getText().toString();
                
                List<PlanExercise> planExercises = new ArrayList<>();
                List<PlanExerciseDraft> drafts = adapter.getItems();
                for (int i = 0; i < drafts.size(); i++) {
                    PlanExerciseDraft draft = drafts.get(i);
                    PlanExercise pe = new PlanExercise();
                    pe.exerciseId = draft.exerciseId;
                    pe.sequenceOrder = i;
                    pe.sets = draft.sets;
                    pe.repsRange = draft.repsRange;
                    planExercises.add(pe);
                }
                
                repository.insertPlan(plan, planExercises);
                finish();
            }
        });
    }
}