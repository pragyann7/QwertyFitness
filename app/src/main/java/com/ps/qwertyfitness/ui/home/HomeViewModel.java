package com.ps.qwertyfitness.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ps.qwertyfitness.data.local.entity.LoggedFood;
import com.ps.qwertyfitness.data.local.entity.UserProfile;
import com.ps.qwertyfitness.data.local.entity.WaterLog;
import com.ps.qwertyfitness.data.local.entity.WeightEntry;
import com.ps.qwertyfitness.data.local.entity.WorkoutPlan;
import com.ps.qwertyfitness.data.repository.FitnessRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.lifecycle.Transformations;

public class HomeViewModel extends AndroidViewModel {
    private final FitnessRepository repository;
    private final LiveData<UserProfile> userProfile;
    private final LiveData<Integer> activeStreak;
    private final LiveData<java.util.Set<String>> workoutDates;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new FitnessRepository(application);
        userProfile = repository.getUserProfile();

        activeStreak = Transformations.switchMap(repository.getAllSessions(), sessions -> repository.getActiveStreak());
        workoutDates = Transformations.switchMap(repository.getAllSessions(), sessions -> repository.getWorkoutDatesForRange());
    }

    public LiveData<java.util.Set<String>> getWorkoutDates() {
        return workoutDates;
    }

    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }

    public LiveData<WeightEntry> getLatestWeight() {
        return repository.getLatestWeight();
    }

    public LiveData<Float> getTotalCaloriesToday() {
        return repository.getTotalCaloriesForDate(getToday());
    }

    public LiveData<Float> getTotalProteinToday() {
        return repository.getTotalProteinForDate(getToday());
    }

    public LiveData<Float> getTotalCarbsToday() {
        return repository.getTotalCarbsForDate(getToday());
    }

    public LiveData<Float> getTotalFatToday() {
        return repository.getTotalFatForDate(getToday());
    }

    public LiveData<List<LoggedFood>> getLoggedFoodsToday() {
        return repository.getLoggedFoodsForDate(getToday());
    }

    public LiveData<List<WorkoutPlan>> getAllPlans() {
        return repository.getAllPlans();
    }

    public LiveData<Integer> getActiveStreak() {
        return activeStreak;
    }

    // Water methods
    public LiveData<Integer> getTotalWaterToday() {
        return repository.getTotalWaterForDate(getToday());
    }

    private String getToday() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    public void logWater(int amountMl) {
        WaterLog log = new WaterLog();
        log.amountMl = amountMl;
        log.date = getToday();
        log.timestamp = System.currentTimeMillis();
        repository.logWater(log);
    }

    public void undoWaterLog() {
        repository.deleteLastWaterLog(getToday());
    }

    public void logActivityCompletion(String activityName) {
        repository.logActivityCompletion(activityName, getToday());
    }

    public void deleteLoggedActivity(String activityName) {
        repository.deleteLoggedActivity(activityName, getToday());
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
