package com.ps.qwertyfitness.ui.progress;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ps.qwertyfitness.R;
import com.ps.qwertyfitness.data.local.entity.UserProfile;
import com.ps.qwertyfitness.databinding.FragmentProgressBinding;

import java.util.Locale;

public class ProgressFragment extends Fragment {
    
    private FragmentProgressBinding binding;
    private ProgressViewModel viewModel;
    private WeightHistoryAdapter historyAdapter;
    private ExercisePRAdapter prAdapter;
    private boolean isShowingAllHistory = false;
    private java.util.List<com.ps.qwertyfitness.data.local.entity.WeightEntry> allWeightEntries = new java.util.ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProgressBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProgressViewModel.class);
        
        historyAdapter = new WeightHistoryAdapter();
        binding.recyclerWeightHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerWeightHistory.setAdapter(historyAdapter);

        prAdapter = new ExercisePRAdapter();
        binding.recyclerPrs.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerPrs.setAdapter(prAdapter);

        setupTimelineFilter();
        
        viewModel.getLatestWeight().observe(getViewLifecycleOwner(), weightEntry -> {
            if (weightEntry != null) {
                binding.textCurrentWeight.setText(String.format(Locale.getDefault(), "%.1f kg", weightEntry.weight));
            } else {
                binding.textCurrentWeight.setText("-- kg");
            }
        });

        viewModel.getAllWeightEntries().observe(getViewLifecycleOwner(), entries -> {
            if (entries != null) {
                allWeightEntries = entries;
                updateHistoryList();
                
                binding.weightGraph.setVisibility(allWeightEntries.isEmpty() ? View.GONE : View.VISIBLE);

                if (entries.size() >= 2) {
                    com.ps.qwertyfitness.data.local.entity.WeightEntry latest = entries.get(entries.size() - 1);
                    com.ps.qwertyfitness.data.local.entity.WeightEntry previous = entries.get(entries.size() - 2);
                    
                    float weightDiff = latest.weight - previous.weight;
                    long timeDiffMs = latest.timestamp - previous.timestamp;
                    long days = timeDiffMs / (24L * 60 * 60 * 1000);
                    
                    String timeText;
                    if (days < 1) {
                        timeText = "since earlier today";
                    } else if (days < 30) {
                        timeText = String.format(Locale.getDefault(), "in %d days", days);
                    } else {
                        long months = days / 30;
                        long remainingDays = days % 30;
                        if (remainingDays == 0) {
                            timeText = String.format(Locale.getDefault(), "in %d month%s", months, months > 1 ? "s" : "");
                        } else {
                            timeText = String.format(Locale.getDefault(), "in %d month%s and %d day%s", 
                                    months, months > 1 ? "s" : "", remainingDays, remainingDays > 1 ? "s" : "");
                        }
                    }
                    
                    String fullText = String.format(Locale.getDefault(), "%s%.1f kg %s", 
                            weightDiff >= 0 ? "+" : "", weightDiff, timeText);
                    
                    binding.textWeightChange.setText(fullText);
                    binding.textWeightChange.setVisibility(View.VISIBLE);
                    
                    binding.textWeightChange.setTextColor(weightDiff <= 0 ? 
                            getResources().getColor(R.color.accent_aqua, null) : 
                            getResources().getColor(R.color.warning_warm_amber, null));
                } else {
                    binding.textWeightChange.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getFilteredGraphData().observe(getViewLifecycleOwner(), entries -> {
            binding.weightGraph.setData(entries != null ? entries : new java.util.ArrayList<>());
        });

        viewModel.getPersonalRecords().observe(getViewLifecycleOwner(), prs -> {
            prAdapter.setItems(prs);
        });

        viewModel.getLatestMeasurement("Chest").observe(getViewLifecycleOwner(), m -> {
            if (m != null) binding.textChestValue.setText(String.format(Locale.getDefault(), "%.1f %s", m.value, m.unit));
        });

        viewModel.getLatestMeasurement("Waist").observe(getViewLifecycleOwner(), m -> {
            if (m != null) binding.textWaistValue.setText(String.format(Locale.getDefault(), "%.1f %s", m.value, m.unit));
        });

        viewModel.getLatestMeasurement("Hips").observe(getViewLifecycleOwner(), m -> {
            if (m != null) binding.textHipsValue.setText(String.format(Locale.getDefault(), "%.1f %s", m.value, m.unit));
        });

        viewModel.getLatestMeasurement("Left Arm").observe(getViewLifecycleOwner(), m -> {
            if (m != null) binding.textLeftArmValue.setText(String.format(Locale.getDefault(), "%.1f %s", m.value, m.unit));
        });

        viewModel.getLatestMeasurement("Right Arm").observe(getViewLifecycleOwner(), m -> {
            if (m != null) binding.textRightArmValue.setText(String.format(Locale.getDefault(), "%.1f %s", m.value, m.unit));
        });

        binding.btnLogWeight.setOnClickListener(v -> {
            LogWeightBottomSheet bottomSheet = new LogWeightBottomSheet();
            bottomSheet.show(getChildFragmentManager(), "LogWeightBottomSheet");
        });

        binding.btnLogMeasurement.setOnClickListener(v -> {
            LogMeasurementBottomSheet bottomSheet = new LogMeasurementBottomSheet();
            bottomSheet.show(getChildFragmentManager(), "LogMeasurementBottomSheet");
        });

        binding.btnViewAllWeight.setOnClickListener(v -> {
            isShowingAllHistory = true;
            updateHistoryList();
            binding.btnViewAllWeight.setVisibility(View.GONE);
        });
    }

    private void updateHistoryList() {
        if (allWeightEntries == null || allWeightEntries.isEmpty()) return;
        
        java.util.List<com.ps.qwertyfitness.data.local.entity.WeightEntry> reversed = new java.util.ArrayList<>(allWeightEntries);
        java.util.Collections.reverse(reversed);
        
        if (!isShowingAllHistory && reversed.size() > 5) {
            historyAdapter.setItems(reversed.subList(0, 5));
            binding.btnViewAllWeight.setVisibility(View.VISIBLE);
        } else {
            historyAdapter.setItems(reversed);
            binding.btnViewAllWeight.setVisibility(View.GONE);
        }
    }

    private void setupTimelineFilter() {
        binding.toggleGroupTimeline.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                String filter = "ALL";
                if (checkedId == R.id.btn_filter_7d) filter = "7D";
                else if (checkedId == R.id.btn_filter_30d) filter = "30D";
                else if (checkedId == R.id.btn_filter_3m) filter = "3M";
                else if (checkedId == R.id.btn_filter_6m) filter = "6M";
                else if (checkedId == R.id.btn_filter_1y) filter = "1Y";
                
                viewModel.setGraphFilter(filter);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}