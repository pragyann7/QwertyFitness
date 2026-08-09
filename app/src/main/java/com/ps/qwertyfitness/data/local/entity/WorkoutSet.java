package com.ps.qwertyfitness.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "workout_sets")
public class WorkoutSet {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public long sessionId;
    public long exerciseId;
    public String exerciseName;
    
    public int setNumber;
    public float weight;
    public int reps;
    public int rpe; // 1-10
    public boolean isCompleted;
    public String note;
}