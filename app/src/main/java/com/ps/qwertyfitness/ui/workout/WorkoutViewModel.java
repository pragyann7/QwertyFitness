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
import com.ps.qwertyfitness.data.local.entity.WorkoutSet;
import com.ps.qwertyfitness.data.repository.FitnessRepository;

import java.util.List;

public class WorkoutViewModel extends AndroidViewModel {
    private final FitnessRepository repository;
    private final MutableLiveData<String> exerciseSearchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> selectedMuscleGroup = new MutableLiveData<>(null);
    private final MutableLiveData<String> selectedEquipment = new MutableLiveData<>(null);
    
    private final LiveData<List<Exercise>> exerciseSearchResults;
    private final LiveData<List<com.ps.qwertyfitness.data.local.entity.ExercisePR>> personalRecords;

    public WorkoutViewModel(@NonNull Application application) {
        super(application);
        repository = new FitnessRepository(application);
        personalRecords = repository.getPersonalRecords();

        androidx.lifecycle.MediatorLiveData<SearchFilter> combinedFilter = new androidx.lifecycle.MediatorLiveData<>();
        combinedFilter.addSource(exerciseSearchQuery, query -> combinedFilter.setValue(new SearchFilter(query, selectedMuscleGroup.getValue(), selectedEquipment.getValue())));
        combinedFilter.addSource(selectedMuscleGroup, muscle -> combinedFilter.setValue(new SearchFilter(exerciseSearchQuery.getValue(), muscle, selectedEquipment.getValue())));
        combinedFilter.addSource(selectedEquipment, equip -> combinedFilter.setValue(new SearchFilter(exerciseSearchQuery.getValue(), selectedMuscleGroup.getValue(), equip)));

        exerciseSearchResults = Transformations.switchMap(combinedFilter, filter -> 
            repository.searchExercisesFiltered(filter.query != null ? filter.query : "", filter.muscleGroup, filter.equipment)
        );
        
        // Initial trigger
        exerciseSearchQuery.setValue("");
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

    public void setMuscleGroupFilter(String muscleGroup) {
        selectedMuscleGroup.setValue(muscleGroup);
    }

    public void setEquipmentFilter(String equipment) {
        selectedEquipment.setValue(equipment);
    }

    public LiveData<List<Exercise>> getExerciseSearchResults() {
        return exerciseSearchResults;
    }

    public LiveData<List<com.ps.qwertyfitness.data.local.entity.ExercisePR>> getPersonalRecords() {
        return personalRecords;
    }

    public LiveData<List<WorkoutSet>> getSetsForSession(long sessionId) {
        return repository.getSetsForSession(sessionId);
    }

    private static class SearchFilter {
        String query;
        String muscleGroup;
        String equipment;

        SearchFilter(String query, String muscleGroup, String equipment) {
            this.query = query;
            this.muscleGroup = muscleGroup;
            this.equipment = equipment;
        }
    }
}
