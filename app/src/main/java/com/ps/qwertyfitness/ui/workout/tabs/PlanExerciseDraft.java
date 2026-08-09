package com.ps.qwertyfitness.ui.workout.tabs;

public class PlanExerciseDraft {
    public long exerciseId;
    public String exerciseName;
    public int sets = 3;
    public String repsRange = "8-12";

    public PlanExerciseDraft(long exerciseId, String exerciseName) {
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
    }
}