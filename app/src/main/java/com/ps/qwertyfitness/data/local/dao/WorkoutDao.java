package com.ps.qwertyfitness.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Transaction;

import com.ps.qwertyfitness.data.local.entity.ExercisePR;
import com.ps.qwertyfitness.data.local.entity.ExerciseProgressPoint;
import com.ps.qwertyfitness.data.local.entity.PlanExercise;
import com.ps.qwertyfitness.data.local.entity.PlanExerciseWithDetails;
import com.ps.qwertyfitness.data.local.entity.WorkoutPlan;
import com.ps.qwertyfitness.data.local.entity.WorkoutSession;
import com.ps.qwertyfitness.data.local.entity.WorkoutSet;
import com.ps.qwertyfitness.data.local.entity.WorkoutSetWithDate;

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

    @Update
    void updatePlan(WorkoutPlan plan);
    
    @Transaction
    @Query("SELECT * FROM plan_exercises WHERE planId = :planId ORDER BY sequenceOrder ASC")
    LiveData<List<PlanExerciseWithDetails>> getExercisesForPlan(long planId);

    @Insert
    long insertSession(WorkoutSession session);
    
    @Update
    void updateSession(WorkoutSession session);

    @Query("UPDATE workout_sessions SET note = :note WHERE id = :sessionId")
    void updateSessionNote(long sessionId, String note);
    
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

    @Query("SELECT MAX(weight) FROM workout_sets WHERE exerciseName = :exerciseName AND isCompleted = 1")
    float getMaxWeightForExerciseSync(String exerciseName);

    @Query("SELECT MAX(reps) FROM workout_sets WHERE exerciseName = :exerciseName AND isCompleted = 1")
    int getMaxRepsForExerciseSync(String exerciseName);

    @Query("SELECT ws.exerciseName, ws.weight as weight, ws.reps as reps, s.startTime as timestamp " +
           "FROM workout_sets ws JOIN workout_sessions s ON ws.sessionId = s.id " +
           "WHERE ws.id IN (SELECT id FROM (SELECT ws2.id, ws2.sessionId FROM workout_sets ws2 WHERE ws2.exerciseName = :exerciseName AND ws2.isCompleted = 1 ORDER BY ws2.weight DESC, ws2.reps DESC) GROUP BY sessionId) " +
           "ORDER BY s.startTime ASC")
    LiveData<List<ExerciseProgressPoint>> getExerciseProgressPoints(String exerciseName);

    @Query("SELECT ws.*, s.date as date FROM workout_sets ws " +
           "JOIN workout_sessions s ON ws.sessionId = s.id " +
           "WHERE ws.exerciseName = :exerciseName AND ws.isCompleted = 1 ORDER BY ws.id DESC")
    LiveData<List<WorkoutSetWithDate>> getExerciseHistoryWithDate(String exerciseName);

    @Query("SELECT ws.exerciseName, ws.weight as maxWeight, ws.reps as maxReps, e.equipment as equipment, " +
           "(SELECT weight FROM workout_sets WHERE exerciseName = ws.exerciseName AND isCompleted = 1 ORDER BY id ASC LIMIT 1) as initialWeight, " +
           "(SELECT reps FROM workout_sets WHERE exerciseName = ws.exerciseName AND isCompleted = 1 ORDER BY id ASC LIMIT 1) as initialReps " +
           "FROM workout_sets ws " +
           "LEFT JOIN exercises e ON ws.exerciseName = e.name " +
           "WHERE ws.id = (SELECT id FROM workout_sets ws2 WHERE ws2.exerciseName = ws.exerciseName AND ws2.isCompleted = 1 ORDER BY weight DESC, reps DESC, id DESC LIMIT 1)")
    LiveData<List<ExercisePR>> getPersonalRecords();

    @Query("DELETE FROM workout_plans WHERE id = :planId")
    void deletePlanById(long planId);

    @Query("DELETE FROM plan_exercises WHERE planId = :planId")
    void deletePlanExercisesByPlanId(long planId);
}