package com.ps.qwertyfitness.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "food_items", indices = {@Index(value = {"name"}, unique = true)})
public class FoodItem {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String name;
    public float calories; 
    public float protein;
    public float carbs;
    public float fat;
    public String category;
    public String unit; // "g", "pc", "ml"
}