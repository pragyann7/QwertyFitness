package com.ps.qwertyfitness.ui.progress;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ps.qwertyfitness.databinding.LayoutLogMeasurementBottomSheetBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogMeasurementBottomSheet extends BottomSheetDialogFragment {

    private LayoutLogMeasurementBottomSheetBinding binding;
    private ProgressViewModel viewModel;
    private final String[] bodyParts = {"Chest", "Waist", "Hips", "Left Arm", "Right Arm", "Left Thigh", "Right Thigh"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutLogMeasurementBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireParentFragment()).get(ProgressViewModel.class);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, bodyParts);
        binding.dropdownBodyPart.setAdapter(adapter);

        binding.btnSaveMeasurement.setOnClickListener(v -> {
            String partName = binding.dropdownBodyPart.getText().toString();
            String valueStr = binding.editValue.getText().toString();
            
            if (!partName.isEmpty() && !valueStr.isEmpty()) {
                float value = Float.parseFloat(valueStr);
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                viewModel.logMeasurement(partName, value, "cm", today);
                dismiss();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}