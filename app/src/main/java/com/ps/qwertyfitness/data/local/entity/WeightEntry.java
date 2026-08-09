package com.ps.qwertyfitness.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "weight_entries")
public class WeightEntry {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public float weight;
    public String date; // YYYY-MM-DD
    public long timestamp;
}