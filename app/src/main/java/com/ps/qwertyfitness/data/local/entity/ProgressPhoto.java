package com.ps.qwertyfitness.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "progress_photos")
public class ProgressPhoto {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String imagePath;
    public String date; // YYYY-MM-DD
    public String category; // Front, Side, Back
    public long timestamp;
}