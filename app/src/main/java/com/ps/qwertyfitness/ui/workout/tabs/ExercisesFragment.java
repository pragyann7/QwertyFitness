package com.ps.qwertyfitness.ui.workout.tabs;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ps.qwertyfitness.R;
import com.ps.qwertyfitness.databinding.FragmentExercisesBinding;
import com.ps.qwertyfitness.ui.workout.WorkoutViewModel;

public class ExercisesFragment extends Fragment {
    
    private FragmentExercisesBinding binding;
    private WorkoutViewModel viewModel;
    private ExerciseAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentExercisesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireParentFragment()).get(WorkoutViewModel.class);
        
        adapter = new ExerciseAdapter();
        binding.recyclerExercises.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerExercises.setAdapter(adapter);
        
        viewModel.getExerciseSearchResults().observe(getViewLifecycleOwner(), exercises -> {
            adapter.setItems(exercises);
        });

        viewModel.getPersonalRecords().observe(getViewLifecycleOwner(), prs -> {
            adapter.setPersonalRecords(prs);
        });
        
        binding.editSearchExercise.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setExerciseSearchQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.chipGroupMuscleFilters.setOnCheckedChangeListener((group, checkedId) -> {
            String muscleGroup = null;
            if (checkedId == R.id.chip_chest) muscleGroup = "Chest";
            else if (checkedId == R.id.chip_back) muscleGroup = "Back";
            else if (checkedId == R.id.chip_shoulders) muscleGroup = "Shoulders";
            else if (checkedId == R.id.chip_biceps) muscleGroup = "Biceps";
            else if (checkedId == R.id.chip_triceps) muscleGroup = "Triceps";
            else if (checkedId == R.id.chip_legs) muscleGroup = "Legs";
            else if (checkedId == R.id.chip_core) muscleGroup = "Core";
            
            viewModel.setMuscleGroupFilter(muscleGroup);
        });

        binding.chipGroupEquipmentFilters.setOnCheckedChangeListener((group, checkedId) -> {
            String equipment = null;
            if (checkedId == R.id.chip_barbell) equipment = "Barbell";
            else if (checkedId == R.id.chip_dumbbell) equipment = "Dumbbell";
            else if (checkedId == R.id.chip_machine) equipment = "Machine";
            else if (checkedId == R.id.chip_cable) equipment = "Cable";
            else if (checkedId == R.id.chip_bodyweight) equipment = "Bodyweight";
            
            viewModel.setEquipmentFilter(equipment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}