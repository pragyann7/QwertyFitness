package com.ps.qwertyfitness.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.ps.qwertyfitness.data.local.entity.Exercise;

import java.util.List;

@Dao
public interface ExerciseDao {
    @Query("SELECT * FROM exercises")
    LiveData<List<Exercise>> getAllExercises();
    
    @Query("SELECT * FROM exercises WHERE targetMuscleGroup = :muscleGroup")
    LiveData<List<Exercise>> getExercisesByMuscleGroup(String muscleGroup);
    
    @Query("SELECT * FROM exercises")
    List<Exercise> getExercisesSync();
    
    @Query("SELECT * FROM exercises WHERE name LIKE :query")
    LiveData<List<Exercise>> searchExercises(String query);

    @Query("SELECT * FROM exercises WHERE name LIKE :query AND (:muscleGroup IS NULL OR targetMuscleGroup = :muscleGroup) AND (:equipment IS NULL OR equipment = :equipment)")
    LiveData<List<Exercise>> searchExercisesFiltered(String query, String muscleGroup, String equipment);

    @Insert(onConflict = androidx.room.OnConflictStrategy.IGNORE)
    long insertSync(Exercise exercise);
    
    @Insert(onConflict = androidx.room.OnConflictStrategy.IGNORE)
    void insert(Exercise exercise);
}
