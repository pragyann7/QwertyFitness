package com.ps.qwertyfitness.ui.profile;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.ps.qwertyfitness.data.local.entity.UserProfile;
import com.ps.qwertyfitness.databinding.ActivityEditProfileBinding;
import com.ps.qwertyfitness.utils.FitnessCalculator;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private ProfileViewModel viewModel;
    
    private final String[] sexes = {"Male", "Female"};
    private final String[] activities = {"Sedentary", "Lightly Active", "Moderately Active", "Very Active"};
    private final String[] goals = {"Lose Fat", "Maintain", "Lean Bulk"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupDropdowns();

        viewModel.getUserProfile().observe(this, profile -> {
            if (profile != null) {
                populateFields(profile);
            }
        });

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    private void setupDropdowns() {
        binding.dropdownSex.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, sexes));
        binding.dropdownActivity.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, activities));
        binding.dropdownGoal.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, goals));
    }

    private void populateFields(UserProfile profile) {
        binding.inputName.setText(profile.name);
        binding.inputAge.setText(String.valueOf(profile.age));
        binding.inputHeight.setText(String.valueOf(profile.height));
        binding.inputWeight.setText(String.valueOf(profile.weight));
        binding.dropdownSex.setText(profile.sex, false);
        binding.dropdownActivity.setText(profile.activityLevel, false);
        binding.dropdownGoal.setText(profile.goal, false);
    }

    private void saveProfile() {
        if (validateInputs()) {
            UserProfile profile = viewModel.getUserProfile().getValue();
            if (profile == null) profile = new UserProfile();
            
            profile.name = binding.inputName.getText().toString();
            profile.age = Integer.parseInt(binding.inputAge.getText().toString());
            profile.sex = binding.dropdownSex.getText().toString();
            profile.height = Float.parseFloat(binding.inputHeight.getText().toString());
            profile.weight = Float.parseFloat(binding.inputWeight.getText().toString());
            profile.activityLevel = binding.dropdownActivity.getText().toString();
            profile.goal = binding.dropdownGoal.getText().toString();

            // Re-calculate all targets locally
            FitnessCalculator.calculateTargets(profile);

            viewModel.updateProfile(profile);
            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs() {
        return !binding.inputName.getText().toString().isEmpty() &&
               !binding.inputAge.getText().toString().isEmpty() &&
               !binding.inputHeight.getText().toString().isEmpty() &&
               !binding.inputWeight.getText().toString().isEmpty();
    }
}
