package com.ps.qwertyfitness.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ps.qwertyfitness.data.local.entity.UserProfile;
import com.ps.qwertyfitness.data.repository.FitnessRepository;
import com.ps.qwertyfitness.utils.FitnessCalculator;

public class ProfileViewModel extends AndroidViewModel {
    private final FitnessRepository repository;
    private final LiveData<UserProfile> userProfile;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new FitnessRepository(application);
        userProfile = repository.getUserProfile();
    }

    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }

    public void updateProfile(UserProfile profile) {
        // Always recalculate targets before saving
        FitnessCalculator.calculateTargets(profile);
        repository.updateProfile(profile);
    }
}