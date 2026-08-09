package com.ps.qwertyfitness.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "body_measurements")
public class BodyMeasurement {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String partName; // Chest, Waist, Hips, etc.
    public float value;
    public String unit; // cm, in
    public String date; // YYYY-MM-DD
    public long timestamp;
}