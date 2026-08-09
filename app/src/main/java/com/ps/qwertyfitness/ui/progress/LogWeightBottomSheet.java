package com.ps.qwertyfitness.ui.progress;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.ps.qwertyfitness.databinding.LayoutLogWeightBottomSheetBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class LogWeightBottomSheet extends BottomSheetDialogFragment {

    private LayoutLogWeightBottomSheetBinding binding;
    private ProgressViewModel viewModel;
    private long selectedDateTimestamp = System.currentTimeMillis();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutLogWeightBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireParentFragment()).get(ProgressViewModel.class);

        binding.btnSelectDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Date")
                    .setSelection(selectedDateTimestamp)
                    .build();
            
            datePicker.addOnPositiveButtonClickListener(selection -> {
                selectedDateTimestamp = selection;
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                binding.btnSelectDate.setText("Date: " + sdf.format(new Date(selection)));
            });
            
            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });

        binding.btnSaveWeight.setOnClickListener(v -> {
            String weightStr = binding.editWeight.getText().toString();
            if (!weightStr.isEmpty()) {
                float weight = Float.parseFloat(weightStr);
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                String dateStr = sdf.format(new Date(selectedDateTimestamp));
                
                viewModel.logWeightWithTimestamp(weight, dateStr, selectedDateTimestamp);
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