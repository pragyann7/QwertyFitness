package com.ps.qwertyfitness.data.local.entity;

import androidx.room.Embedded;

public class WorkoutSetWithDate {
    @Embedded
    public WorkoutSet workoutSet;
    public String date;
}
