package com.ps.qwertyfitness.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Transaction;

import com.ps.qwertyfitness.data.local.entity.ExercisePR;
import com.ps.qwertyfitness.data.local.entity.PlanExercise;
import com.ps.qwertyfitness.data.local.entity.PlanExerciseWithDetails;
import com.ps.qwertyfitness.data.local.entity.WorkoutPlan;
import com.ps.qwertyfitness.data.local.entity.WorkoutSession;
import com.ps.qwertyfitness.data.local.entity.WorkoutSet;

import java.util.List;

@Dao
public interface WorkoutDao {
    @Query("SELECT * FROM workout_plans")
    LiveData<List<WorkoutPlan>> getAllPlans();
    
    @Query("SELECT * FROM workout_plans")
    List<WorkoutPlan> getAllPlansSync();
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertPlan(WorkoutPlan plan);
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertPlanExercise(PlanExercise planExercise);
    
    @Transaction
    @Query("SELECT * FROM plan_exercises WHERE planId = :planId ORDER BY sequenceOrder ASC")
    LiveData<List<PlanExerciseWithDetails>> getExercisesForPlan(long planId);

    @Insert
    long insertSession(WorkoutSession session);
    
    @Update
    void updateSession(WorkoutSession session);
    
    @Insert
    void insertSet(WorkoutSet set);
    
    @Update
    void updateSet(WorkoutSet set);
    
    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC")
    LiveData<List<WorkoutSession>> getAllSessions();
    
    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId")
    LiveData<List<WorkoutSet>> getSetsForSession(long sessionId);

    @Query("SELECT * FROM workout_sets WHERE exerciseName = :exerciseName AND isCompleted = 1 ORDER BY id DESC LIMIT 1")
    WorkoutSet getLatestSetForExercise(String exerciseName);

    @Query("SELECT exerciseName, MAX(weight) as maxWeight FROM workout_sets WHERE isCompleted = 1 GROUP BY exerciseName")
    LiveData<List<ExercisePR>> getPersonalRecords();
}