package com.ps.qwertyfitness.ui.workout.tabs;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ps.qwertyfitness.data.local.entity.Exercise;
import com.ps.qwertyfitness.data.local.entity.PlanExercise;
import com.ps.qwertyfitness.data.local.entity.WorkoutPlan;
import com.ps.qwertyfitness.data.repository.FitnessRepository;
import com.ps.qwertyfitness.databinding.ActivityCreatePlanBinding;
import com.ps.qwertyfitness.utils.ReminderManager;
import com.ps.qwertyfitness.data.local.entity.Reminder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreatePlanActivity extends AppCompatActivity {

    private ActivityCreatePlanBinding binding;
    private AddedExerciseAdapter adapter;
    private FitnessRepository repository;
    private long editingPlanId = -1;
    private final String[] difficultyLevels = {"Beginner", "Intermediate", "Advanced"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreatePlanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        repository = new FitnessRepository(getApplication());
        editingPlanId = getIntent().getLongExtra("PLAN_ID", -1);
        
        ArrayAdapter<String> diffAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, difficultyLevels);
        binding.dropdownDifficulty.setAdapter(diffAdapter);
        binding.dropdownDifficulty.setText(difficultyLevels[1], false); // Default to Intermediate

        if (editingPlanId != -1) {
            loadPlanForEditing(editingPlanId);
            binding.btnSavePlan.setText("UPDATE PLAN");
        }

        binding.editPlanReminderTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new android.app.TimePickerDialog(this, (view, hourOfDay, minute) -> {
                binding.editPlanReminderTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        });

        adapter = new AddedExerciseAdapter();
        binding.recyclerPlanExercises.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerPlanExercises.setAdapter(adapter);
        
        binding.btnAddExercise.setOnClickListener(v -> {
            ExercisePickerBottomSheet picker = new ExercisePickerBottomSheet();
            picker.setListener(exercises -> {
                for (Exercise exercise : exercises) {
                    adapter.addExercise(new PlanExerciseDraft(exercise.id, exercise.name));
                }
            });
            picker.show(getSupportFragmentManager(), "ExercisePicker");
        });
        
        binding.btnSavePlan.setOnClickListener(v -> {
            String name = binding.editPlanName.getText().toString();
            
            // Get selected days
            StringBuilder daysBuilder = new StringBuilder();
            int daysCount = 0;
            if (binding.chipMon.isChecked()) { daysBuilder.append("1,"); daysCount++; }
            if (binding.chipTue.isChecked()) { daysBuilder.append("2,"); daysCount++; }
            if (binding.chipWed.isChecked()) { daysBuilder.append("3,"); daysCount++; }
            if (binding.chipThu.isChecked()) { daysBuilder.append("4,"); daysCount++; }
            if (binding.chipFri.isChecked()) { daysBuilder.append("5,"); daysCount++; }
            if (binding.chipSat.isChecked()) { daysBuilder.append("6,"); daysCount++; }
            if (binding.chipSun.isChecked()) { daysBuilder.append("7,"); daysCount++; }
            
            String selectedDays = daysBuilder.length() > 0 ? daysBuilder.substring(0, daysBuilder.length() - 1) : "";

            if (!name.isEmpty()) {
                WorkoutPlan plan = new WorkoutPlan();
                if (editingPlanId != -1) plan.id = editingPlanId;
                plan.name = name;
                plan.trainingDaysPerWeek = daysCount;
                plan.selectedDays = selectedDays;
                plan.difficulty = binding.dropdownDifficulty.getText().toString();
                plan.isRecommended = false;
                
                // Set reminder time
                String reminderTime = binding.editPlanReminderTime.getText().toString();
                plan.reminderTime = reminderTime;
                
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
                
                if (editingPlanId != -1) {
                    repository.updatePlan(plan, planExercises);
                } else {
                    repository.insertPlan(plan, planExercises);
                }

                // Auto-create/update reminder if time is set
                if (reminderTime != null && !reminderTime.isEmpty()) {
                    Reminder reminder = new Reminder();
                    reminder.title = "Workout: " + name;
                    reminder.time = reminderTime;
                    reminder.type = "WORKOUT";
                    reminder.repeatDays = selectedDays;
                    reminder.enabled = true;
                    repository.insertReminder(reminder, () -> {
                        ReminderManager.scheduleReminder(this, reminder);
                    });
                }

                finish();
            }
        });
    }

    private void loadPlanForEditing(long planId) {
        repository.getExercisesForPlan(planId).observe(this, details -> {
            if (details != null && !details.isEmpty() && adapter.getItemCount() == 0) {
                WorkoutPlan plan = details.get(0).plan;
                binding.editPlanName.setText(plan.name);
                binding.dropdownDifficulty.setText(plan.difficulty, false);
                binding.editPlanReminderTime.setText(plan.reminderTime);
                
                // Set days
                if (plan.selectedDays != null) {
                    String[] days = plan.selectedDays.split(",");
                    for (String d : days) {
                        if (d.equals("1")) binding.chipMon.setChecked(true);
                        else if (d.equals("2")) binding.chipTue.setChecked(true);
                        else if (d.equals("3")) binding.chipWed.setChecked(true);
                        else if (d.equals("4")) binding.chipThu.setChecked(true);
                        else if (d.equals("5")) binding.chipFri.setChecked(true);
                        else if (d.equals("6")) binding.chipSat.setChecked(true);
                        else if (d.equals("7")) binding.chipSun.setChecked(true);
                    }
                }
                
                // Add exercises
                for (com.ps.qwertyfitness.data.local.entity.PlanExerciseWithDetails d : details) {
                    PlanExerciseDraft draft = new PlanExerciseDraft(d.exercise.id, d.exercise.name);
                    draft.sets = d.planExercise.sets;
                    draft.repsRange = d.planExercise.repsRange;
                    adapter.addExercise(draft);
                }
            }
        });
    }
}
