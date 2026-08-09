package com.ps.qwertyfitness.ui.workout.active;

import android.app.Application;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ps.qwertyfitness.data.local.entity.PlanExerciseWithDetails;
import com.ps.qwertyfitness.data.local.entity.WorkoutSession;
import com.ps.qwertyfitness.data.local.entity.WorkoutSet;
import com.ps.qwertyfitness.data.repository.FitnessRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActiveWorkoutViewModel extends AndroidViewModel {
    private final FitnessRepository repository;
    
    private final MutableLiveData<Long> restTimeRemaining = new MutableLiveData<>(0L);
    private final MutableLiveData<Boolean> isTimerRunning = new MutableLiveData<>(false);
    
    private WorkoutSession currentSession;
    private CountDownTimer restTimer;

    public ActiveWorkoutViewModel(@NonNull Application application) {
        super(application);
        repository = new FitnessRepository(application);
    }

    public void startWorkout(String planName) {
        currentSession = new WorkoutSession();
        currentSession.planName = planName;
        currentSession.date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        currentSession.startTime = System.currentTimeMillis();
        
        // In a real app, we'd insert this into the DB and get the ID
        // For now, let's assume we have it.
    }

    public void startRestTimer(long durationMillis) {
        if (restTimer != null) {
            restTimer.cancel();
        }
        
        isTimerRunning.setValue(true);
        restTimer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                restTimeRemaining.setValue(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                isTimerRunning.setValue(false);
                restTimeRemaining.setValue(0L);
            }
        }.start();
    }

    public void skipTimer() {
        if (restTimer != null) {
            restTimer.cancel();
            isTimerRunning.setValue(false);
            restTimeRemaining.setValue(0L);
        }
    }

    public LiveData<Long> getRestTimeRemaining() {
        return restTimeRemaining;
    }

    public LiveData<Boolean> getIsTimerRunning() {
        return isTimerRunning;
    }

    public LiveData<List<PlanExerciseWithDetails>> getExercisesForPlan(long planId) {
        return repository.getExercisesForPlan(planId);
    }

    public void loadPreviousData(List<ActiveExercise> activeExercises, Runnable onComplete) {
        new Thread(() -> {
            for (ActiveExercise ae : activeExercises) {
                WorkoutSet last = repository.getLatestSetForExercise(ae.name);
                if (last != null) {
                    ae.previousSession = String.format(Locale.getDefault(), "Last: %.1f kg x %d", last.weight, last.reps);
                }
            }
            if (onComplete != null) onComplete.run();
        }).start();
    }

    public void finishWorkout(List<ActiveExercise> activeExercises) {
        if (currentSession == null) return;
        
        currentSession.endTime = System.currentTimeMillis();
        
        List<WorkoutSet> allSets = new ArrayList<>();
        float totalVolume = 0;
        int totalSets = 0;
        
        for (ActiveExercise ae : activeExercises) {
            for (WorkoutSet set : ae.sets) {
                if (set.isCompleted) {
                    set.exerciseName = ae.name;
                    allSets.add(set);
                    totalVolume += (set.weight * set.reps);
                    totalSets++;
                }
            }
        }
        
        currentSession.totalVolume = (int) totalVolume;
        currentSession.totalSets = totalSets;
        
        repository.insertSession(currentSession, allSets);
    }
}
