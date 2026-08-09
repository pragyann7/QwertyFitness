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
            picker.setListener(exercise -> adapter.addExercise(new PlanExerciseDraft(exercise.id, exercise.name)));
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
                plan.name = name;
                plan.trainingDaysPerWeek = daysCount;
                plan.selectedDays = selectedDays;
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

                // Auto-create reminder if time is set
                String reminderTime = binding.editPlanReminderTime.getText().toString();
                if (!reminderTime.isEmpty()) {
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
}