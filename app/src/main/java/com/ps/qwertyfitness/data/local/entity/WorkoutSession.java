package com.ps.qwertyfitness.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "workout_sessions")
public class WorkoutSession {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String planName;
    public long planId;
    public String date; // YYYY-MM-DD
    public long startTime; // Epoch ms
    public long endTime; // Epoch ms
    public int totalVolume;
    public int totalSets;
    public int totalPRs;
    public String note;
}