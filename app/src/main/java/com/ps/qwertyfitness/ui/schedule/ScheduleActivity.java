package com.ps.qwertyfitness.ui.schedule;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ps.qwertyfitness.R;
import com.ps.qwertyfitness.data.local.entity.Reminder;
import com.ps.qwertyfitness.databinding.ActivityScheduleBinding;
import com.ps.qwertyfitness.ui.home.HomeViewModel;
import com.ps.qwertyfitness.utils.ReminderManager;

import java.util.Calendar;
import java.util.Locale;

public class ScheduleActivity extends AppCompatActivity {

    private ActivityScheduleBinding binding;
    private HomeViewModel viewModel;
    private ReminderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding.btnBack.setOnClickListener(v -> finish());

        adapter = new ReminderAdapter(new ReminderAdapter.OnReminderChangeListener() {
            @Override
            public void onToggle(Reminder reminder, boolean isEnabled) {
                reminder.enabled = isEnabled;
                viewModel.updateReminder(reminder);
                if (isEnabled) {
                    ReminderManager.scheduleReminder(ScheduleActivity.this, reminder);
                } else {
                    ReminderManager.cancelReminder(ScheduleActivity.this, reminder);
                }
            }

            @Override
            public void onDelete(Reminder reminder) {
                new MaterialAlertDialogBuilder(ScheduleActivity.this)
                        .setTitle("Delete Reminder")
                        .setMessage("Remove this reminder?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            ReminderManager.cancelReminder(ScheduleActivity.this, reminder);
                            viewModel.deleteReminder(reminder);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onEdit(Reminder reminder) {
                showReminderDialog(reminder);
            }
        });

        binding.recyclerReminders.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerReminders.setAdapter(adapter);

        viewModel.getAllReminders().observe(this, reminders -> {
            adapter.submitList(reminders);
        });

        binding.fabAddReminder.setOnClickListener(v -> showReminderDialog(null));
    }

    private void showReminderDialog(Reminder existingReminder) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_reminder, null);
        AutoCompleteTextView dropdownType = view.findViewById(R.id.dropdown_type);
        EditText editTitle = view.findViewById(R.id.edit_reminder_title);
        EditText editTime = view.findViewById(R.id.edit_reminder_time);
        EditText editEndTime = view.findViewById(R.id.edit_reminder_end_time);
        EditText editInterval = view.findViewById(R.id.edit_reminder_interval);
        EditText editTargetCals = view.findViewById(R.id.edit_target_calories);
        EditText editTargetProt = view.findViewById(R.id.edit_target_protein);
        EditText editTargetCarb = view.findViewById(R.id.edit_target_carbs);
        EditText editTargetFat = view.findViewById(R.id.edit_target_fat);
        View layoutEndTime = view.findViewById(R.id.layout_end_time);
        View layoutInterval = view.findViewById(R.id.layout_interval);
        View layoutMealTargets = view.findViewById(R.id.layout_meal_targets);
        ChipGroup chipGroupDays = view.findViewById(R.id.chip_group_days);

        String[] types = {"Workout", "Meal", "Water", "Weight"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, types);
        dropdownType.setAdapter(typeAdapter);

        final String[] selectedType = {existingReminder != null ? existingReminder.type : "WORKOUT"};
        
        // Pre-fill data if editing
        if (existingReminder != null) {
            editTitle.setText(existingReminder.title);
            editTime.setText(existingReminder.time);
            
            // Find type index
            int typePos = 0;
            String[] internalTypes = {"WORKOUT", "MEAL", "WATER", "WEIGHT"};
            for (int i = 0; i < internalTypes.length; i++) {
                if (internalTypes[i].equals(existingReminder.type)) {
                    typePos = i;
                    break;
                }
            }
            dropdownType.setText(types[typePos], false);
            
            if ("WATER".equals(existingReminder.type)) {
                layoutInterval.setVisibility(View.VISIBLE);
                layoutEndTime.setVisibility(View.VISIBLE);
                if (existingReminder.intervalMinutes > 0) editInterval.setText(String.valueOf(existingReminder.intervalMinutes));
                if (existingReminder.endTime != null) editEndTime.setText(existingReminder.endTime);
            } else if ("MEAL".equals(existingReminder.type)) {
                layoutMealTargets.setVisibility(View.VISIBLE);
                if (existingReminder.targetValue > 0) editTargetCals.setText(String.valueOf(existingReminder.targetValue));
                if (existingReminder.targetProtein > 0) editTargetProt.setText(String.valueOf(existingReminder.targetProtein));
                if (existingReminder.targetCarbs > 0) editTargetCarb.setText(String.valueOf(existingReminder.targetCarbs));
                if (existingReminder.targetFat > 0) editTargetFat.setText(String.valueOf(existingReminder.targetFat));
            }

            if (existingReminder.repeatDays != null) {
                String[] days = existingReminder.repeatDays.split(",");
                for (String d : days) {
                    if (!d.trim().isEmpty()) {
                        int dayIdx = Integer.parseInt(d.trim()) - 1;
                        if (dayIdx >= 0 && dayIdx < chipGroupDays.getChildCount()) {
                            ((Chip) chipGroupDays.getChildAt(dayIdx)).setChecked(true);
                        }
                    }
                }
            }
        }

        dropdownType.setOnItemClickListener((parent, view1, position, id) -> {
            String[] internalTypes = {"WORKOUT", "MEAL", "WATER", "WEIGHT"};
            selectedType[0] = internalTypes[position];
            
            boolean isWater = "WATER".equals(selectedType[0]);
            boolean isMeal = "MEAL".equals(selectedType[0]);
            layoutInterval.setVisibility(isWater ? View.VISIBLE : View.GONE);
            layoutEndTime.setVisibility(isWater ? View.VISIBLE : View.GONE);
            layoutMealTargets.setVisibility(isMeal ? View.VISIBLE : View.GONE);
        });

        editTime.setOnClickListener(v -> pickTime(editTime));
        editEndTime.setOnClickListener(v -> pickTime(editEndTime));

        new MaterialAlertDialogBuilder(this)
                .setTitle(existingReminder != null ? "Edit Reminder" : "Add Reminder")
                .setView(view)
                .setPositiveButton(existingReminder != null ? "Save" : "Add", (dialog, which) -> {
                    String title = editTitle.getText().toString();
                    String time = editTime.getText().toString();
                    String endTime = editEndTime.getText().toString();
                    String intervalStr = editInterval.getText().toString();
                    
                    if (!title.isEmpty() && !time.isEmpty()) {
                        Reminder reminder = existingReminder != null ? existingReminder : new Reminder();
                        reminder.title = title;
                        reminder.time = time;
                        reminder.type = selectedType[0];
                        
                        if (existingReminder == null) {
                            reminder.enabled = true;
                        }
                        
                        if ("WATER".equals(reminder.type)) {
                            reminder.intervalMinutes = !intervalStr.isEmpty() ? Integer.parseInt(intervalStr) : 0;
                            reminder.endTime = !endTime.isEmpty() ? endTime : null;
                        } else {
                            reminder.intervalMinutes = 0;
                            reminder.endTime = null;
                        }

                        if ("MEAL".equals(reminder.type)) {
                            reminder.targetValue = !editTargetCals.getText().toString().isEmpty() ? Integer.parseInt(editTargetCals.getText().toString()) : 0;
                            reminder.targetProtein = !editTargetProt.getText().toString().isEmpty() ? Integer.parseInt(editTargetProt.getText().toString()) : 0;
                            reminder.targetCarbs = !editTargetCarb.getText().toString().isEmpty() ? Integer.parseInt(editTargetCarb.getText().toString()) : 0;
                            reminder.targetFat = !editTargetFat.getText().toString().isEmpty() ? Integer.parseInt(editTargetFat.getText().toString()) : 0;
                        } else {
                            reminder.targetValue = 0;
                            reminder.targetProtein = 0;
                            reminder.targetCarbs = 0;
                            reminder.targetFat = 0;
                        }

                        StringBuilder days = new java.lang.StringBuilder();
                        for (int i = 0; i < chipGroupDays.getChildCount(); i++) {
                            Chip chip = (Chip) chipGroupDays.getChildAt(i);
                            if (chip.isChecked()) {
                                days.append(i + 1).append(",");
                            }
                        }
                        
                        if (days.length() > 0) {
                            reminder.repeatDays = days.substring(0, days.length() - 1);
                        } else {
                            reminder.repeatDays = null;
                        }

                        if (existingReminder != null) {
                            viewModel.updateReminder(reminder);
                            ReminderManager.cancelReminder(ScheduleActivity.this, reminder);
                            if (reminder.enabled) {
                                ReminderManager.scheduleReminder(ScheduleActivity.this, reminder);
                            }
                        } else {
                            viewModel.insertReminder(reminder, () -> {
                                ReminderManager.scheduleReminder(ScheduleActivity.this, reminder);
                            });
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void pickTime(EditText editText) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            editText.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }
}
