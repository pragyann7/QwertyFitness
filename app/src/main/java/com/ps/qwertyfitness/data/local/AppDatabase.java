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
import com.ps.qwertyfitness.data.local.dao.UserDao;
import com.ps.qwertyfitness.data.local.dao.WeightDao;
import com.ps.qwertyfitness.data.local.dao.WorkoutDao;
import com.ps.qwertyfitness.data.local.entity.BodyMeasurement;
import com.ps.qwertyfitness.data.local.entity.Exercise;
import com.ps.qwertyfitness.data.local.entity.FoodItem;
import com.ps.qwertyfitness.data.local.entity.LoggedFood;
import com.ps.qwertyfitness.data.local.entity.PlanExercise;
import com.ps.qwertyfitness.data.local.entity.ProgressPhoto;
import com.ps.qwertyfitness.data.local.entity.UserProfile;
import com.ps.qwertyfitness.data.local.entity.WeightEntry;
import com.ps.qwertyfitness.data.local.entity.WorkoutPlan;
import com.ps.qwertyfitness.data.local.entity.WorkoutSession;
import com.ps.qwertyfitness.data.local.entity.WorkoutSet;

import java.util.concurrent.Executors;

@Database(entities = {UserProfile.class, WorkoutPlan.class, Exercise.class, FoodItem.class, LoggedFood.class, WorkoutSession.class, WorkoutSet.class, WeightEntry.class, PlanExercise.class, BodyMeasurement.class, ProgressPhoto.class}, version = 12, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    
    public abstract UserDao userDao();
    public abstract ExerciseDao exerciseDao();
    public abstract FoodDao foodDao();
    public abstract WorkoutDao workoutDao();
    public abstract WeightDao weightDao();
    public abstract BodyMeasurementDao bodyMeasurementDao();
    public abstract ProgressPhotoDao progressPhotoDao();
    
    private static volatile AppDatabase INSTANCE;
    
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "qwerty_fitness_database")
                            .addCallback(sRoomDatabaseCallback)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            seedData();
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            seedData();
        }
    };

    private static void seedData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (INSTANCE == null) return;

            ExerciseDao dao = INSTANCE.exerciseDao();
            WorkoutDao workoutDao = INSTANCE.workoutDao();
            FoodDao foodDao = INSTANCE.foodDao();

            // Seed Exercises if missing
            if (dao.getExercisesSync().isEmpty()) {
                dao.insertSync(createExercise("Bench Press", "Chest", "Barbell"));
                dao.insertSync(createExercise("Squat", "Legs", "Barbell"));
                dao.insertSync(createExercise("Deadlift", "Back/Legs", "Barbell"));
            }

            // Seed Plans if missing
            if (workoutDao.getAllPlansSync().isEmpty()) {
                // Ensure we have IDs for linking
                long e1Id = getExerciseId(dao, "Bench Press");
                long e2Id = getExerciseId(dao, "Squat");
                long e3Id = getExerciseId(dao, "Deadlift");

                if (e1Id != -1 && e2Id != -1 && e3Id != -1) {
                    WorkoutPlan p3 = new WorkoutPlan();
                    p3.name = "Full Body";
                    p3.trainingDaysPerWeek = 3;
                    p3.difficulty = "Beginner";
                    long p3Id = workoutDao.insertPlan(p3);
                    
                    workoutDao.insertPlanExercise(createPlanExercise(p3Id, e1Id, 3, "8-12", 0));
                    workoutDao.insertPlanExercise(createPlanExercise(p3Id, e2Id, 3, "8-12", 1));
                    workoutDao.insertPlanExercise(createPlanExercise(p3Id, e3Id, 3, "8-12", 2));
                }
            }

            // Seed Food if missing
            if (foodDao.getAllFoodItemsSync().isEmpty()) {
                foodDao.insertFoodItem(createFood("Chicken Breast", 165, 31, 0, 3.6f, "Meat"));
                foodDao.insertFoodItem(createFood("Brown Rice", 111, 2.6f, 23, 0.9f, "Grains"));
                foodDao.insertFoodItem(createFood("Oatmeal", 68, 2.4f, 12, 1.4f, "Grains"));
                foodDao.insertFoodItem(createFood("Whole Egg", 155, 13, 1.1f, 11, "Dairy"));
            }
        });
    }

    private static Exercise createExercise(String name, String muscle, String equip) {
        Exercise e = new Exercise();
        e.name = name;
        e.targetMuscleGroup = muscle;
        e.equipment = equip;
        return e;
    }

    private static long getExerciseId(ExerciseDao dao, String name) {
        for (Exercise e : dao.getExercisesSync()) {
            if (e.name.equals(name)) return e.id;
        }
        return -1;
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

    private static FoodItem createFood(String name, float cal, float pro, float carb, float fat, String cat) {
        FoodItem f = new FoodItem();
        f.name = name;
        f.calories = cal;
        f.protein = pro;
        f.carbs = carb;
        f.fat = fat;
        f.category = cat;
        return f;
    }
}
