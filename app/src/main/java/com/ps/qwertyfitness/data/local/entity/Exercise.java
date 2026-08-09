package com.ps.qwertyfitness.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "exercises", indices = {@Index(value = {"name"}, unique = true)})
public class Exercise {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String name;
    public String targetMuscleGroup;
    public String equipment;
    public String category; // Barbell, Dumbbell, etc.
}