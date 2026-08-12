package com.ps.qwertyfitness.data.local.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

public class PlanExerciseWithDetails {
    @Embedded
    public PlanExercise planExercise;

    @Relation(
            parentColumn = "exerciseId",
            entityColumn = "id"
    )
    public Exercise exercise;

    @Relation(
            parentColumn = "planId",
            entityColumn = "id"
    )
    public WorkoutPlan plan;
}