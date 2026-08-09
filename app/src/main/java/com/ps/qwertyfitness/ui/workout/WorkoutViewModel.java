package com.ps.qwertyfitness.ui.workout;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.ps.qwertyfitness.data.local.entity.Exercise;
import com.ps.qwertyfitness.data.local.entity.WorkoutPlan;
import com.ps.qwertyfitness.data.local.entity.WorkoutSession;
import com.ps.qwertyfitness.data.repository.FitnessRepository;

import java.util.ArrayList;
import java.util.List;

public class WorkoutViewModel extends AndroidViewModel {
    private final FitnessRepository repository;
    private final MutableLiveData<String> exerciseSearchQuery = new MutableLiveData<>("");
    private final LiveData<List<Exercise>> exerciseSearchResults;

    public WorkoutViewModel(@NonNull Application application) {
        super(application);
        repository = new FitnessRepository(application);
        
        exerciseSearchResults = Transformations.switchMap(exerciseSearchQuery, query -> {
            if (query == null || query.isEmpty()) {
                return repository.searchExercises(""); // Return all
            }
            return repository.searchExercises(query);
        });
    }

    public LiveData<List<WorkoutPlan>> getAllPlans() {
        return repository.getAllPlans();
    }

    public LiveData<List<WorkoutSession>> getAllSessions() {
        return repository.getAllSessions();
    }

    public LiveData<List<com.ps.qwertyfitness.data.local.entity.PlanExerciseWithDetails>> getExercisesForPlan(long planId) {
        return repository.getExercisesForPlan(planId);
    }

    public void setExerciseSearchQuery(String query) {
        exerciseSearchQuery.setValue(query);
    }

    public LiveData<List<Exercise>> getExerciseSearchResults() {
        return exerciseSearchResults;
    }
}
