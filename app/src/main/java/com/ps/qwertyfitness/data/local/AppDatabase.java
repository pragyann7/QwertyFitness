package com.ps.qwertyfitness.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.ps.qwertyfitness.data.local.dao.BodyMeasurementDao;
import com.ps.qwertyfitness.data.local.dao.ExerciseDao;
import com.ps.qwertyfitness.data.local.dao.FoodDao;
import com.ps.qwertyfitness.data.local.dao.ProgressPhotoDao;
import com.ps.qwertyfitness.data.local.dao.ReminderDao;
import com.ps.qwertyfitness.data.local.dao.UserDao;
import com.ps.qwertyfitness.data.local.dao.WaterDao;
import com.ps.qwertyfitness.data.local.dao.WeightDao;
import com.ps.qwertyfitness.data.local.dao.WorkoutDao;
import com.ps.qwertyfitness.data.local.entity.BodyMeasurement;
import com.ps.qwertyfitness.data.local.entity.Exercise;
import com.ps.qwertyfitness.data.local.entity.FoodItem;
import com.ps.qwertyfitness.data.local.entity.LoggedFood;
import com.ps.qwertyfitness.data.local.entity.PlanExercise;
import com.ps.qwertyfitness.data.local.entity.ProgressPhoto;
import com.ps.qwertyfitness.data.local.entity.Reminder;
import com.ps.qwertyfitness.data.local.entity.UserProfile;
import com.ps.qwertyfitness.data.local.entity.WaterLog;
import com.ps.qwertyfitness.data.local.entity.WeightEntry;
import com.ps.qwertyfitness.data.local.entity.WorkoutPlan;
import com.ps.qwertyfitness.data.local.entity.WorkoutSession;
import com.ps.qwertyfitness.data.local.entity.WorkoutSet;

import java.util.concurrent.Executors;

@Database(entities = {UserProfile.class, WorkoutPlan.class, Exercise.class, FoodItem.class, LoggedFood.class, WorkoutSession.class, WorkoutSet.class, WeightEntry.class, PlanExercise.class, BodyMeasurement.class, ProgressPhoto.class, Reminder.class, WaterLog.class}, version = 29, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    
    public abstract UserDao userDao();
    public abstract ExerciseDao exerciseDao();
    public abstract FoodDao foodDao();
    public abstract WorkoutDao workoutDao();
    public abstract WeightDao weightDao();
    public abstract BodyMeasurementDao bodyMeasurementDao();
    public abstract ProgressPhotoDao progressPhotoDao();
    public abstract ReminderDao reminderDao();
    public abstract WaterDao waterDao();
    
    private static volatile AppDatabase INSTANCE;
    
    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Context is needed for widget updates
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            // Data is seeded in getDatabase to ensure context is available
        }
    };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "qwerty_fitness_database")
                            .addCallback(sRoomDatabaseCallback)
                            .fallbackToDestructiveMigration()
                            .build();
                    seedData(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    private static void seedData(Context context) {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (INSTANCE == null) return;

            ExerciseDao dao = INSTANCE.exerciseDao();
            WorkoutDao workoutDao = INSTANCE.workoutDao();
            FoodDao foodDao = INSTANCE.foodDao();

            // 1. Seed Exercises
            // Push
            long pushupId = insertExerciseIfMissing(dao, "Standard / Decline Push-ups", "Chest", "Bodyweight");
            long floorPressId = insertExerciseIfMissing(dao, "Dumbbell Floor Press", "Chest", "Dumbbell");
            long pikePushupId = insertExerciseIfMissing(dao, "Pike Push-ups", "Shoulders", "Bodyweight");
            long latRaiseId = insertExerciseIfMissing(dao, "Dumbbell Lateral Raises", "Shoulders", "Dumbbell");
            long chairDipId = insertExerciseIfMissing(dao, "Chair Dips", "Triceps", "Chair");
            long shoulderTapId = insertExerciseIfMissing(dao, "Plank to Shoulder Taps", "Core", "Bodyweight");

            // Pull
            long dbRowId = insertExerciseIfMissing(dao, "Bent-Over Dumbbell Rows", "Back", "Dumbbell");
            long singleDbRowId = insertExerciseIfMissing(dao, "Single-Arm Dumbbell Rows", "Back", "Dumbbell");
            long proneYId = insertExerciseIfMissing(dao, "Prone Y-Raises", "Back", "Bodyweight");
            long bicepCurlId = insertExerciseIfMissing(dao, "Dumbbell Bicep Curls", "Biceps", "Dumbbell");
            long hammerCurlId = insertExerciseIfMissing(dao, "Hammer Curls", "Biceps", "Dumbbell");
            long towelRowId = insertExerciseIfMissing(dao, "Towel Rows", "Back", "Cable");

            // Legs
            long bulgarianId = insertExerciseIfMissing(dao, "Bulgarian Split Squats", "Legs", "Dumbbell");
            long gobletSquatId = insertExerciseIfMissing(dao, "Dumbbell Goblet Squats", "Legs", "Dumbbell");
            long rdlId = insertExerciseIfMissing(dao, "Dumbbell Romanian Deadlifts", "Legs", "Dumbbell");
            long lungeId = insertExerciseIfMissing(dao, "Walking Lunges", "Legs", "Dumbbell");
            long calfRaiseId = insertExerciseIfMissing(dao, "Single-Leg Calf Raises", "Calves", "Bodyweight");

            // Upper Body (Additional)
            long elevatedPushupId = insertExerciseIfMissing(dao, "Feet-Elevated Push-ups", "Chest", "Bodyweight");
            long rearDeltFlyId = insertExerciseIfMissing(dao, "Rear-Delt Flyes", "Shoulders", "Dumbbell");
            long tricepExtId = insertExerciseIfMissing(dao, "Overhead Dumbbell Triceps Extensions", "Triceps", "Dumbbell");
            long concCurlId = insertExerciseIfMissing(dao, "Concentration Curls", "Biceps", "Dumbbell");

            // Lower Body + Core (Additional)
            long singleLegRdlId = insertExerciseIfMissing(dao, "Single-Leg Dumbbell Romanian Deadlifts", "Legs", "Dumbbell");
            long gluteBridgeId = insertExerciseIfMissing(dao, "Glute Bridges", "Glutes", "Bodyweight");
            long standingCalfRaiseId = insertExerciseIfMissing(dao, "Standing Calf Raises", "Calves", "Bodyweight");
            long legRaiseId = insertExerciseIfMissing(dao, "Lying Leg Raises", "Core", "Bodyweight");
            long plankId = insertExerciseIfMissing(dao, "Forearm Planks", "Core", "Bodyweight");

            // 2. Seed Plans
            if (workoutDao.getAllPlansSync().isEmpty()) {
                // Full Body
                WorkoutPlan fullBody = new WorkoutPlan();
                fullBody.name = "Full Body";
                fullBody.trainingDaysPerWeek = 3;
                fullBody.difficulty = "Intermediate";
                fullBody.isRecommended = true;
                long fullBodyId = workoutDao.insertPlan(fullBody);
                workoutDao.insertPlanExercise(createPlanExercise(fullBodyId, pushupId, 3, "10-15", 0));
                workoutDao.insertPlanExercise(createPlanExercise(fullBodyId, dbRowId, 3, "8-12", 1));
                workoutDao.insertPlanExercise(createPlanExercise(fullBodyId, gobletSquatId, 3, "10-12", 2));
                workoutDao.insertPlanExercise(createPlanExercise(fullBodyId, lungeId, 3, "10 each", 3));
                workoutDao.insertPlanExercise(createPlanExercise(fullBodyId, plankId, 3, "60 sec", 4));

                // Leg
                WorkoutPlan legsPlan = new WorkoutPlan();
                legsPlan.name = "Leg";
                legsPlan.trainingDaysPerWeek = 1;
                legsPlan.difficulty = "Intermediate";
                legsPlan.isRecommended = true;
                long legsPlanId = workoutDao.insertPlan(legsPlan);
                workoutDao.insertPlanExercise(createPlanExercise(legsPlanId, gobletSquatId, 3, "10-12", 0));
                workoutDao.insertPlanExercise(createPlanExercise(legsPlanId, bulgarianId, 3, "8-10", 1));
                workoutDao.insertPlanExercise(createPlanExercise(legsPlanId, rdlId, 3, "10-12", 2));
                workoutDao.insertPlanExercise(createPlanExercise(legsPlanId, gluteBridgeId, 3, "15-20", 3));
                workoutDao.insertPlanExercise(createPlanExercise(legsPlanId, calfRaiseId, 3, "15-20", 4));

                // Chest & Arm
                WorkoutPlan chestArmPlan = new WorkoutPlan();
                chestArmPlan.name = "Chest & Arm";
                chestArmPlan.trainingDaysPerWeek = 1;
                chestArmPlan.difficulty = "Intermediate";
                chestArmPlan.isRecommended = true;
                long chestArmPlanId = workoutDao.insertPlan(chestArmPlan);
                workoutDao.insertPlanExercise(createPlanExercise(chestArmPlanId, pushupId, 3, "12-15", 0));
                workoutDao.insertPlanExercise(createPlanExercise(chestArmPlanId, floorPressId, 3, "8-12", 1));
                workoutDao.insertPlanExercise(createPlanExercise(chestArmPlanId, bicepCurlId, 3, "10-12", 2));
                workoutDao.insertPlanExercise(createPlanExercise(chestArmPlanId, tricepExtId, 3, "10-12", 3));
                workoutDao.insertPlanExercise(createPlanExercise(chestArmPlanId, hammerCurlId, 3, "10-12", 4));

                // Abs
                WorkoutPlan absPlan = new WorkoutPlan();
                absPlan.name = "Abs";
                absPlan.trainingDaysPerWeek = 1;
                absPlan.difficulty = "Beginner";
                absPlan.isRecommended = true;
                long absPlanId = workoutDao.insertPlan(absPlan);
                workoutDao.insertPlanExercise(createPlanExercise(absPlanId, legRaiseId, 3, "12-15", 0));
                workoutDao.insertPlanExercise(createPlanExercise(absPlanId, shoulderTapId, 3, "20 total", 1));
                workoutDao.insertPlanExercise(createPlanExercise(absPlanId, plankId, 3, "60 sec", 2));
            }

            // Seed Food
            foodDao.insertFoodItem(createFood("Chicken Breast", 165, 31, 0, 3.6f, "Meat", "g"));
            foodDao.insertFoodItem(createFood("Brown Rice", 111, 2.6f, 23, 0.9f, "Grains", "g"));
            foodDao.insertFoodItem(createFood("Oatmeal", 68, 2.4f, 12, 1.4f, "Grains", "g"));
            foodDao.insertFoodItem(createFood("Whole Egg", 78, 6.3f, 0.6f, 5, "Dairy", "pc"));
            
            // New Foods
            foodDao.insertFoodItem(createFood("Wheat Flour Crepe (Roti)", 120, 4, 22, 2, "Grains", "pc"));
            foodDao.insertFoodItem(createFood("Milk", 80, 3.2f, 4.5f, 4.2f, "Dairy", "ml"));
            foodDao.insertFoodItem(createFood("Curd", 61, 3.5f, 4.7f, 3.3f, "Dairy", "g"));
            foodDao.insertFoodItem(createFood("White Milled Rice", 130, 2.7f, 28, 0.3f, "Grains", "g"));
            foodDao.insertFoodItem(createFood("Moong Lentils", 105, 7, 19, 0.4f, "Legumes", "g"));
            foodDao.insertFoodItem(createFood("Maas Lentils", 105, 7.5f, 18, 0.5f, "Legumes", "g"));
            foodDao.insertFoodItem(createFood("Raw Black Chickpeas", 360, 19, 61, 6, "Legumes", "g"));
            foodDao.insertFoodItem(createFood("Soaked Soya Chunks", 100, 15, 8, 0.5f, "Plant Protein", "g"));
            foodDao.insertFoodItem(createFood("Gourd Potato Curry", 80, 1.5f, 12, 4, "Vegetable", "g"));
            foodDao.insertFoodItem(createFood("Cauliflower Potato Curry", 90, 2, 13, 5, "Vegetable", "g"));
            foodDao.insertFoodItem(createFood("Buffalo Meat", 143, 24, 0, 4.8f, "Meat", "g"));
            foodDao.insertFoodItem(createFood("Fish", 100, 20, 0, 1.5f, "Meat", "g"));
            
            // Extra Foods
            foodDao.insertFoodItem(createFood("Banana", 105, 1.3f, 27, 0.4f, "Fruit", "pc"));
            foodDao.insertFoodItem(createFood("Peanut Butter", 94, 4, 3, 8, "Fats", "g"));
            foodDao.insertFoodItem(createFood("Greek Yogurt", 100, 10, 4, 5, "Dairy", "g"));
            foodDao.insertFoodItem(createFood("Tofu", 76, 8, 1.9f, 4.8f, "Plant Protein", "g"));
            foodDao.insertFoodItem(createFood("Apple", 95, 0.5f, 25, 0.3f, "Fruit", "pc"));
            foodDao.insertFoodItem(createFood("Almonds (10pcs)", 70, 2.5f, 2.5f, 6, "Nuts", "pc"));

            // Refresh widget after reset
            com.ps.qwertyfitness.widgets.DailyProgressWidget.updateAllWidgets(context);
        });
    }

    private static long insertExerciseIfMissing(ExerciseDao dao, String name, String muscle, String equip) {
        Exercise e = new Exercise();
        e.name = name;
        e.targetMuscleGroup = muscle;
        e.equipment = equip;
        long id = dao.insertSync(e);
        if (id == -1) {
            // Already exists, find ID
            for (Exercise existing : dao.getExercisesSync()) {
                if (existing.name.equals(name)) return existing.id;
            }
        }
        return id;
    }

    private static PlanExercise createPlanExercise(long pId, long eId, int sets, String reps, int order) {
        PlanExercise pe = new PlanExercise();
        pe.planId = pId;
        pe.exerciseId = eId;
        pe.sets = sets;
        pe.repsRange = reps;
        pe.sequenceOrder = order;
        return pe;
    }

    private static FoodItem createFood(String name, float cal, float pro, float carb, float fat, String cat, String unit) {
        FoodItem f = new FoodItem();
        f.name = name;
        f.calories = cal;
        f.protein = pro;
        f.carbs = carb;
        f.fat = fat;
        f.category = cat;
        f.unit = unit;
        return f;
    }
}
