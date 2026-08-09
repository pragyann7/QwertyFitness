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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}