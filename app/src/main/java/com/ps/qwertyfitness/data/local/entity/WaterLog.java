package com.ps.qwertyfitness.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "water_logs")
public class WaterLog {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public int amountMl;
    public String date; // YYYY-MM-DD
    public long timestamp;
}
