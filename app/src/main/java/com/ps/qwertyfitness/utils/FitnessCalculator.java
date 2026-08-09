package com.ps.qwertyfitness.utils;

import com.ps.qwertyfitness.data.local.entity.UserProfile;

public class FitnessCalculator {

    public static UserProfile calculateTargets(UserProfile profile) {
        // Mifflin-St Jeor Equation
        float bmr;
        if ("Male".equalsIgnoreCase(profile.sex)) {
            bmr = (10 * profile.weight) + (6.25f * profile.height) - (5 * profile.age) + 5;
        } else {
            bmr = (10 * profile.weight) + (6.25f * profile.height) - (5 * profile.age) - 161;
        }

        float multiplier;
        switch (profile.activityLevel) {
            case "Sedentary": multiplier = 1.2f; break;
            case "Lightly Active": multiplier = 1.375f; break;
            case "Moderately Active": multiplier = 1.55f; break;
            case "Very Active": multiplier = 1.725f; break;
            default: multiplier = 1.2f;
        }

        float tdee = bmr * multiplier;

        // Goal Adjustment
        int targetCalories;
        if ("Lose Fat".equalsIgnoreCase(profile.goal)) {
            targetCalories = (int) (tdee - 500);
        } else if ("Lean Bulk".equalsIgnoreCase(profile.goal)) {
            targetCalories = (int) (tdee + 300);
        } else {
            targetCalories = (int) tdee;
        }

        // Macro calculation
        // Protein: 2g per kg of body weight
        int proteinTarget = (int) (profile.weight * 2.0f);
        
        // Fat: 25% of total calories (9 calories per gram)
        int fatTarget = (int) ((targetCalories * 0.25f) / 9);
        
        // Carbs: Remainder of calories (4 calories per gram)
        int proteinCals = proteinTarget * 4;
        int fatCals = fatTarget * 9;
        int carbTarget = (targetCalories - proteinCals - fatCals) / 4;

        profile.dailyCalorieTarget = targetCalories;
        profile.proteinTarget = proteinTarget;
        profile.fatTarget = fatTarget;
        profile.carbTarget = carbTarget;

        return profile;
    }
}