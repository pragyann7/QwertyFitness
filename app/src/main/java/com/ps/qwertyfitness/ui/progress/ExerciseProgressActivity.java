package com.ps.qwertyfitness.ui.progress;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ps.qwertyfitness.data.local.entity.ExerciseProgressPoint;
import com.ps.qwertyfitness.data.local.entity.WorkoutSetWithDate;
import com.ps.qwertyfitness.databinding.ActivityExerciseProgressBinding;

import java.util.ArrayList;
import java.util.List;

public class ExerciseProgressActivity extends AppCompatActivity {

    private ActivityExerciseProgressBinding binding;
    private ProgressViewModel viewModel;
    private SetHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExerciseProgressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ProgressViewModel.class);

        String exerciseName = getIntent().getStringExtra("EXERCISE_NAME");
        binding.toolbar.setTitle(exerciseName);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new SetHistoryAdapter();
        binding.recyclerExerciseHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerExerciseHistory.setAdapter(adapter);

        viewModel.getExerciseDetails(exerciseName).observe(this, exercise -> {
            String nameLower = exerciseName != null ? exerciseName.toLowerCase() : "";
            boolean isBodyweight = (exercise != null && "Bodyweight".equalsIgnoreCase(exercise.equipment)) ||
                                   nameLower.contains("push-up") || 
                                   nameLower.contains("push up") ||
                                   nameLower.contains("plank") ||
                                   nameLower.contains("sit-up") ||
                                   nameLower.contains("leg raise");
            
            viewModel.getExerciseProgressPoints(exerciseName).observe(this, points -> {
                if (points != null) {
                    List<TrendGraphView.DataPoint> graphPoints = new ArrayList<>();
                    for (ExerciseProgressPoint p : points) {
                        float value = (isBodyweight && p.weight == 0) ? p.reps : p.weight;
                        graphPoints.add(new TrendGraphView.DataPoint(p.timestamp, value));
                    }
                    binding.exerciseGraph.setData(graphPoints, (isBodyweight && !graphPoints.isEmpty() && graphPoints.get(0).value > 50) ? "reps" : (isBodyweight ? "reps/kg" : "kg"));

                    // Better logic for label
                    String label = "kg";
                    if (isBodyweight) {
                        boolean hasWeight = false;
                        for (ExerciseProgressPoint p : points) if (p.weight > 0) hasWeight = true;
                        label = hasWeight ? "Extra kg / reps" : "reps";
                    }
                    binding.exerciseGraph.setData(graphPoints, label);
                }
            });
        });

        viewModel.getExerciseHistory(exerciseName).observe(this, sets -> {
            adapter.setItems(sets);
        });
    }
}
