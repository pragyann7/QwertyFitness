package com.ps.qwertyfitness.ui.workout.tabs;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ps.qwertyfitness.data.local.entity.WorkoutSet;
import com.ps.qwertyfitness.databinding.ActivitySessionDetailBinding;
import com.ps.qwertyfitness.ui.workout.WorkoutViewModel;
import com.ps.qwertyfitness.ui.workout.active.ActiveExercise;
import com.ps.qwertyfitness.ui.workout.active.SummaryExerciseAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SessionDetailActivity extends AppCompatActivity {

    private ActivitySessionDetailBinding binding;
    private WorkoutViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySessionDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(WorkoutViewModel.class);

        long sessionId = getIntent().getLongExtra("SESSION_ID", -1);
        String planName = getIntent().getStringExtra("PLAN_NAME");
        String date = getIntent().getStringExtra("DATE");
        int volume = getIntent().getIntExtra("VOLUME", 0);
        int setsCount = getIntent().getIntExtra("SETS", 0);
        int prs = getIntent().getIntExtra("PRS", 0);
        long durationMillis = getIntent().getLongExtra("DURATION", 0);
        String notes = getIntent().getStringExtra("NOTES");

        binding.textSessionName.setText(planName);
        binding.textSessionDate.setText(date);
        
        long minutes = durationMillis / (60 * 1000);
        binding.textDetailDuration.setText(String.format(Locale.getDefault(), "%d min", minutes));
        binding.textDetailVolume.setText(String.format(Locale.getDefault(), "%,d kg", volume));
        binding.textDetailSets.setText(String.valueOf(setsCount));
        binding.textDetailPrs.setText(String.valueOf(prs));

        if (notes != null && !notes.isEmpty()) {
            binding.textSessionNotes.setText(notes);
        } else {
            binding.textSessionNotes.setText("No notes for this session.");
        }

        binding.btnBack.setOnClickListener(v -> finish());

        SummaryExerciseAdapter adapter = new SummaryExerciseAdapter();
        binding.recyclerDetailExercises.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerDetailExercises.setAdapter(adapter);

        viewModel.getSetsForSession(sessionId).observe(this, sets -> {
            if (sets != null) {
                adapter.setItems(groupSetsByExercise(sets));
            }
        });
    }

    private List<ActiveExercise> groupSetsByExercise(List<WorkoutSet> sets) {
        Map<String, ActiveExercise> groupMap = new HashMap<>();
        List<ActiveExercise> result = new ArrayList<>();

        for (WorkoutSet set : sets) {
            ActiveExercise ae = groupMap.get(set.exerciseName);
            if (ae == null) {
                ae = new ActiveExercise(set.exerciseName, set.exerciseId, "", "");
                groupMap.put(set.exerciseName, ae);
                result.add(ae);
            }
            ae.sets.add(set);
        }
        return result;
    }
}
