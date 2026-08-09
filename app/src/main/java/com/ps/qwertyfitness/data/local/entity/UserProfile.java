package com.ps.qwertyfitness.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_profile")
public class UserProfile {
    @PrimaryKey
    public int id = 1; // Single user app
    
    public String name;
    public int age;
    public String sex;
    public float height;
    public float weight;
    public String activityLevel;
    public String goal; // Lose Fat, Maintain, Lean Bulk
    
    // Calculated targets
    public int dailyCalorieTarget;
    public int proteinTarget;
    public int carbTarget;
    public int fatTarget;
}