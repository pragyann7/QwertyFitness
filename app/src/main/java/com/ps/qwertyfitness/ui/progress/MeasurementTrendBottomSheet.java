package com.ps.qwertyfitness.ui.progress;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ps.qwertyfitness.data.local.entity.BodyMeasurement;
import com.ps.qwertyfitness.databinding.LayoutMeasurementTrendBottomSheetBinding;

import java.util.ArrayList;
import java.util.List;

public class MeasurementTrendBottomSheet extends BottomSheetDialogFragment {

    private LayoutMeasurementTrendBottomSheetBinding binding;
    private final String partName;

    public MeasurementTrendBottomSheet(String partName) {
        this.partName = partName;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutMeasurementTrendBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ProgressViewModel viewModel = new ViewModelProvider(this).get(ProgressViewModel.class);

        binding.textMeasurementTitle.setText(partName + " Progress");
        binding.btnClose.setOnClickListener(v -> dismiss());

        viewModel.getMeasurementHistory(partName).observe(getViewLifecycleOwner(), measurements -> {
            if (measurements != null) {
                List<TrendGraphView.DataPoint> points = new ArrayList<>();
                String unit = "";
                // Data is DESC, reverse for graph
                for (int i = measurements.size() - 1; i >= 0; i--) {
                    BodyMeasurement m = measurements.get(i);
                    points.add(new TrendGraphView.DataPoint(m.timestamp, m.value));
                    unit = m.unit;
                }
                binding.measurementGraph.setData(points, unit);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
