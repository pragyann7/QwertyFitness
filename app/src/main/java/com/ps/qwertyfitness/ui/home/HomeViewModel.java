package com.ps.qwertyfitness.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ps.qwertyfitness.data.local.entity.LoggedFood;
import com.ps.qwertyfitness.data.local.entity.UserProfile;
import com.ps.qwertyfitness.data.local.entity.WeightEntry;
import com.ps.qwertyfitness.data.repository.FitnessRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeViewModel extends AndroidViewModel {
    private final FitnessRepository repository;
    private final LiveData<UserProfile> userProfile;
    private final String today;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new FitnessRepository(application);
        userProfile = repository.getUserProfile();
        today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }

    public LiveData<WeightEntry> getLatestWeight() {
        return repository.getLatestWeight();
    }

    public LiveData<Float> getTotalCaloriesToday() {
        return repository.getTotalCaloriesForDate(today);
    }

    public LiveData<Float> getTotalProteinToday() {
        return repository.getTotalProteinForDate(today);
    }

    public LiveData<Float> getTotalCarbsToday() {
        return repository.getTotalCarbsForDate(today);
    }

    public LiveData<Float> getTotalFatToday() {
        return repository.getTotalFatForDate(today);
    }

    public LiveData<List<LoggedFood>> getLoggedFoodsToday() {
        return repository.getLoggedFoodsForDate(today);
    }

    // Reminder methods
    public LiveData<List<com.ps.qwertyfitness.data.local.entity.Reminder>> getAllReminders() {
        return repository.getAllReminders();
    }

    public void insertReminder(com.ps.qwertyfitness.data.local.entity.Reminder reminder, Runnable onDone) {
        repository.insertReminder(reminder, onDone);
    }

    public void updateReminder(com.ps.qwertyfitness.data.local.entity.Reminder reminder) {
        repository.updateReminder(reminder);
    }

    public void deleteReminder(com.ps.qwertyfitness.data.local.entity.Reminder reminder) {
        repository.deleteReminder(reminder);
    }
}
