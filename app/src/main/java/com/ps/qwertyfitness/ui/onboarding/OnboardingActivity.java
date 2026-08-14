package com.ps.qwertyfitness.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import android.widget.ArrayAdapter;

import com.ps.qwertyfitness.MainActivity;
import com.ps.qwertyfitness.data.local.entity.UserProfile;
import com.ps.qwertyfitness.data.repository.FitnessRepository;
import com.ps.qwertyfitness.databinding.ActivityOnboardingBinding;
import com.ps.qwertyfitness.utils.FitnessCalculator;

public class OnboardingActivity extends AppCompatActivity {
    
    private ActivityOnboardingBinding binding;
    private FitnessRepository repository;
    private final String[] sexes = {"Male", "Female"};
    private final String[] activities = {"Sedentary", "Lightly Active", "Moderately Active", "Very Active"};
    private final String[] goals = {"Lose Fat", "Maintain", "Lean Bulk"};
    private boolean isReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        
        // Force a widget refresh on startup to clear any stale data (e.g. after clearing app data)
        com.ps.qwertyfitness.widgets.DailyProgressWidget.updateAllWidgets(getApplicationContext());

        repository = new FitnessRepository(getApplication());
        
        splashScreen.setKeepOnScreenCondition(() -> !isReady);

        repository.getUserProfile().observe(this, profile -> {
            isReady = true;
            if (profile != null) {
                Intent mainIntent = new Intent(this, MainActivity.class);
                // Forward any action from widget (e.g. START_WORKOUT)
                if (getIntent().hasExtra("ACTION")) {
                    mainIntent.putExtra("ACTION", getIntent().getStringExtra("ACTION"));
                }
                startActivity(mainIntent);
                finish();
            }
        });

        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setupDropdowns();
        
        binding.btnFinish.setOnClickListener(v -> {
            if (validateInputs()) {
                UserProfile profile = new UserProfile();
                profile.name = binding.inputName.getText().toString();
                profile.age = Integer.parseInt(binding.inputAge.getText().toString());
                profile.sex = binding.dropdownSex.getText().toString();
                profile.height = Float.parseFloat(binding.inputHeight.getText().toString());
                profile.weight = Float.parseFloat(binding.inputWeight.getText().toString());
                profile.activityLevel = binding.dropdownActivity.getText().toString();
                profile.goal = binding.dropdownGoal.getText().toString();
                
                // Perform calculations
                FitnessCalculator.calculateTargets(profile);
                
                repository.insertProfile(profile);
                
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });
    }

    private void setupDropdowns() {
        binding.dropdownSex.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, sexes));
        binding.dropdownActivity.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, activities));
        binding.dropdownGoal.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, goals));
    }

    private boolean validateInputs() {
        return !binding.inputName.getText().toString().isEmpty() &&
               !binding.inputAge.getText().toString().isEmpty() &&
               !binding.inputHeight.getText().toString().isEmpty() &&
               !binding.inputWeight.getText().toString().isEmpty() &&
               !binding.dropdownSex.getText().toString().isEmpty() &&
               !binding.dropdownActivity.getText().toString().isEmpty() &&
               !binding.dropdownGoal.getText().toString().isEmpty();
    }
}
