package com.ps.qwertyfitness.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "logged_foods")
public class LoggedFood {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public long foodItemId;
    public String foodName;
    public float quantity; // in grams/ml
    public float calories;
    public float protein;
    public float carbs;
    public float fat;
    
    public String date; // YYYY-MM-DD
    public String mealType; // Breakfast, Lunch, Snack, Dinner
    public boolean isActivity; // True if this is an activity completion log (e.g. workout), not food
}