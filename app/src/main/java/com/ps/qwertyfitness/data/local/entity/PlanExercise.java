package com.ps.qwertyfitness.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "plan_exercises",
        foreignKeys = {
                @ForeignKey(entity = WorkoutPlan.class, 
                            parentColumns = "id", 
                            childColumns = "planId", 
                            onDelete = CASCADE),
                @ForeignKey(entity = Exercise.class, 
                            parentColumns = "id", 
                            childColumns = "exerciseId", 
                            onDelete = CASCADE)
        })
public class PlanExercise {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long planId;
    public long exerciseId;
    
    public int sequenceOrder;
    public int sets;
    public String repsRange; // e.g., "8-12"
}