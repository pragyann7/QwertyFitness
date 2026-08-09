package com.ps.qwertyfitness.ui.workout.tabs;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ps.qwertyfitness.databinding.FragmentPlansBinding;
import com.ps.qwertyfitness.ui.workout.WorkoutViewModel;
import com.ps.qwertyfitness.ui.workout.active.ActiveWorkoutActivity;

public class PlansFragment extends Fragment {
    
    private FragmentPlansBinding binding;
    private WorkoutViewModel viewModel;
    private PlansAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPlansBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireParentFragment()).get(WorkoutViewModel.class);
        
        adapter = new PlansAdapter(plan -> {
            Intent intent = new Intent(getActivity(), PlanDetailActivity.class);
            intent.putExtra("PLAN_NAME", plan.name);
            intent.putExtra("PLAN_ID", plan.id);
            startActivity(intent);
        });
        
        binding.recyclerPlans.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerPlans.setAdapter(adapter);
        
        viewModel.getAllPlans().observe(getViewLifecycleOwner(), plans -> {
            adapter.setItems(plans);
        });

        binding.btnCreatePlan.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreatePlanActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}