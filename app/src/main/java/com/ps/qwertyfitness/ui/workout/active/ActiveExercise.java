package com.ps.qwertyfitness.ui.workout.active;

import com.ps.qwertyfitness.data.local.entity.WorkoutSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ActiveExercise implements Serializable {
    public String name;
    public long exerciseId;
    public String targetReps;
    public String previousSession = "No previous data";
    public List<WorkoutSet> sets = new ArrayList<>();

    public ActiveExercise(String name, long exerciseId, String targetReps) {
        this.name = name;
        this.exerciseId = exerciseId;
        this.targetReps = targetReps;
    }
}