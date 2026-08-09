package com.ps.qwertyfitness.ui.diet;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ps.qwertyfitness.data.local.entity.FoodItem;
import com.ps.qwertyfitness.data.local.entity.LoggedFood;
import com.ps.qwertyfitness.data.local.entity.UserProfile;
import com.ps.qwertyfitness.data.repository.FitnessRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

public class DietViewModel extends AndroidViewModel {
    private final FitnessRepository repository;
    private final LiveData<UserProfile> userProfile;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final LiveData<List<FoodItem>> searchResults;
    private final String today;

    public DietViewModel(@NonNull Application application) {
        super(application);
        repository = new FitnessRepository(application);
        userProfile = repository.getUserProfile();
        today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        searchResults = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.isEmpty()) {
                return repository.searchFood(""); // Show all if empty
            }
            return repository.searchFood(query);
        });
    }

    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }

    public LiveData<List<LoggedFood>> getLoggedFoodsToday() {
        return repository.getLoggedFoodsForDate(today);
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

    public void logFood(LoggedFood loggedFood) {
        repository.logFood(loggedFood);
    }

    public void deleteLoggedFood(LoggedFood loggedFood) {
        repository.deleteLoggedFood(loggedFood);
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public LiveData<List<FoodItem>> getSearchResults() {
        return searchResults;
    }
}
