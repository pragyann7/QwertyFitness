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

import com.ps.qwertyfitness.data.local.entity.WorkoutPlan;
import com.ps.qwertyfitness.databinding.FragmentPlansBinding;
import com.ps.qwertyfitness.ui.workout.WorkoutViewModel;

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
        
        adapter = new PlansAdapter(new PlansAdapter.OnPlanClickListener() {
            @Override
            public void onPlanClick(WorkoutPlan plan) {
                Intent intent = new Intent(getActivity(), PlanDetailActivity.class);
                intent.putExtra("PLAN_NAME", plan.name);
                intent.putExtra("PLAN_ID", plan.id);
                intent.putExtra("IS_RECOMMENDED", plan.isRecommended);
                startActivity(intent);
            }

            @Override
            public void onEditModeChanged(boolean editMode) {
                binding.layoutSelectionBar.setVisibility(editMode ? View.VISIBLE : View.GONE);
                binding.btnCreatePlan.setVisibility(editMode ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onSelectionChanged(int count) {
                binding.textSelectionCount.setText(count + " selected");
            }
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

        binding.btnCancelSelection.setOnClickListener(v -> {
            adapter.setEditMode(false);
            binding.layoutSelectionBar.setVisibility(View.GONE);
            binding.btnCreatePlan.setVisibility(View.VISIBLE);
        });

        binding.btnDeleteSelected.setOnClickListener(v -> {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Plans")
                    .setMessage("Are you sure you want to delete the selected plans?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // Pass a copy of the set to avoid race conditions when clearing
                        java.util.Set<Long> idsToDelete = new java.util.HashSet<>(adapter.getSelectedPlanIds());
                        viewModel.deletePlans(idsToDelete);
                        adapter.setEditMode(false);
                        binding.layoutSelectionBar.setVisibility(View.GONE);
                        binding.btnCreatePlan.setVisibility(View.VISIBLE);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
