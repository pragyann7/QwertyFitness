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

import com.ps.qwertyfitness.databinding.FragmentHistoryBinding;
import com.ps.qwertyfitness.ui.workout.WorkoutViewModel;

public class HistoryFragment extends Fragment {
    
    private FragmentHistoryBinding binding;
    private WorkoutViewModel viewModel;
    private SessionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireParentFragment()).get(WorkoutViewModel.class);
        
        adapter = new SessionAdapter(session -> {
            Intent intent = new Intent(getActivity(), SessionDetailActivity.class);
            intent.putExtra("SESSION_ID", session.id);
            intent.putExtra("PLAN_NAME", session.planName);
            intent.putExtra("DATE", session.date);
            intent.putExtra("VOLUME", session.totalVolume);
            intent.putExtra("SETS", session.totalSets);
            intent.putExtra("DURATION", (session.endTime - session.startTime));
            intent.putExtra("NOTES", session.note);
            startActivity(intent);
        });
        binding.recyclerHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerHistory.setAdapter(adapter);
        
        viewModel.getAllSessions().observe(getViewLifecycleOwner(), sessions -> {
            adapter.setItems(sessions);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}