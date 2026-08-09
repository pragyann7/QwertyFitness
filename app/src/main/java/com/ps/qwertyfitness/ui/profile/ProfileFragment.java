package com.ps.qwertyfitness.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ps.qwertyfitness.databinding.FragmentProfileBinding;

import java.util.Locale;

public class ProfileFragment extends Fragment {
    
    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                binding.textUserName.setText(profile.name);
                binding.textUserGoal.setText(profile.goal);
                binding.textUserWeight.setText(String.format(Locale.getDefault(), "%.1f kg", profile.weight));
                binding.textUserHeight.setText(String.format(Locale.getDefault(), "%.0f cm", profile.height));
                binding.textUserAge.setText(String.valueOf(profile.age));
                
                binding.textTargetCalories.setText(String.format(Locale.getDefault(), "%,d kcal", profile.dailyCalorieTarget));
                binding.textTargetProtein.setText(String.format(Locale.getDefault(), "%d g", profile.proteinTarget));
                binding.textTargetCarbs.setText(String.format(Locale.getDefault(), "%d g", profile.carbTarget));
                binding.textTargetFat.setText(String.format(Locale.getDefault(), "%d g", profile.fatTarget));
            }
        });
        
        binding.btnEditProfile.setOnClickListener(v -> {
            // Placeholder for Edit Profile Dialog
            // For now, let's allow a quick goal swap to test the ripple effect
            com.ps.qwertyfitness.data.local.entity.UserProfile current = viewModel.getUserProfile().getValue();
            if (current != null) {
                if ("Lose Fat".equals(current.goal)) current.goal = "Lean Bulk";
                else current.goal = "Lose Fat";
                viewModel.updateProfile(current);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}