package com.ps.qwertyfitness.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "workout_plans", indices = {@Index(value = {"name"}, unique = true)})
public class WorkoutPlan {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String name;
    public String description;
    public int trainingDaysPerWeek;
    public String difficulty;
}