package com.ps.qwertyfitness.ui.workout.active;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ps.qwertyfitness.databinding.ActivityWorkoutSummaryBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorkoutSummaryActivity extends AppCompatActivity {

    private ActivityWorkoutSummaryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWorkoutSummaryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String planName = getIntent().getStringExtra("PLAN_NAME");
        long durationMillis = getIntent().getLongExtra("DURATION", 0);
        int volume = getIntent().getIntExtra("VOLUME", 0);
        int sets = getIntent().getIntExtra("SETS", 0);
        int prs = getIntent().getIntExtra("PRS", 0);
        List<ActiveExercise> exercises = (List<ActiveExercise>) getIntent().getSerializableExtra("EXERCISES");

        binding.textWorkoutName.setText(planName);
        
        long minutes = durationMillis / (60 * 1000);
        binding.textSummaryDuration.setText(String.format(Locale.getDefault(), "%d min", minutes));
        binding.textSummaryVolume.setText(String.format(Locale.getDefault(), "%,d kg", volume));
        binding.textSummarySets.setText(String.valueOf(sets));
        binding.textSummaryPrs.setText(String.valueOf(prs));

        SummaryExerciseAdapter adapter = new SummaryExerciseAdapter();
        binding.recyclerSummaryExercises.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSummaryExercises.setAdapter(adapter);
        adapter.setItems(exercises);

        binding.btnDone.setOnClickListener(v -> {
            String note = binding.editSummaryNotes.getText().toString();
            long sessionId = getIntent().getLongExtra("SESSION_ID", -1);
            if (!note.isEmpty() && sessionId != -1) {
                new Thread(() -> {
                    com.ps.qwertyfitness.data.local.AppDatabase db = com.ps.qwertyfitness.data.local.AppDatabase.getDatabase(this);
                    com.ps.qwertyfitness.data.local.entity.WorkoutSession session = new com.ps.qwertyfitness.data.local.entity.WorkoutSession();
                    session.id = sessionId;
                    session.planName = getIntent().getStringExtra("PLAN_NAME");
                    session.totalVolume = getIntent().getIntExtra("VOLUME", 0);
                    session.totalSets = getIntent().getIntExtra("SETS", 0);
                    session.totalPRs = getIntent().getIntExtra("PRS", 0);
                    session.startTime = System.currentTimeMillis() - getIntent().getLongExtra("DURATION", 0);
                    session.endTime = System.currentTimeMillis();
                    session.date = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date());
                    session.note = note;
                    db.workoutDao().updateSession(session);
                }).start();
            }
            finish();
        });
    }
}
