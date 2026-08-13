package com.ps.qwertyfitness.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reminders")
public class Reminder {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String type; // WORKOUT, MEAL, WATER, WEIGHT, MEASUREMENT, PHOTO, SLEEP
    public String title;
    public String time; // HH:mm
    public String repeatDays; // e.g., "1,2,3,4,5" (1=Mon, 7=Sun)
    public boolean enabled;
    public int targetValue; // For water (ml), or target kcal for meal
    public int intervalMinutes; // 0 for once a day, > 0 for repeating (e.g., 20)
    public String endTime; // HH:mm (Optional, for interval reminders)
    public long snoozeUntil; // Timestamp until which the reminder is snoozed
    
    public Long planId; // Linked workout plan ID
    
    public int targetProtein;
    public int targetCarbs;
    public int targetFat;
}
