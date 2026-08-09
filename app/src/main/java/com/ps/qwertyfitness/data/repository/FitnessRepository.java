package com.ps.qwertyfitness.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.ps.qwertyfitness.data.local.AppDatabase;
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
import com.ps.qwertyfitness.data.local.entity.PlanExerciseWithDetails;
import com.ps.qwertyfitness.data.local.entity.ProgressPhoto;
import com.ps.qwertyfitness.data.local.entity.UserProfile;
import com.ps.qwertyfitness.data.local.entity.WeightEntry;
import com.ps.qwertyfitness.data.local.entity.WorkoutPlan;
import com.ps.qwertyfitness.data.local.entity.WorkoutSession;
import com.ps.qwertyfitness.data.local.entity.WorkoutSet;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FitnessRepository {
    private final UserDao userDao;
    private final FoodDao foodDao;
    private final WorkoutDao workoutDao;
    private final WeightDao weightDao;
    private final ExerciseDao exerciseDao;
    private final BodyMeasurementDao bodyMeasurementDao;
    private final ProgressPhotoDao progressPhotoDao;
    private final LiveData<UserProfile> userProfile;
    private final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public FitnessRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
        foodDao = db.foodDao();
        workoutDao = db.workoutDao();
        weightDao = db.weightDao();
        exerciseDao = db.exerciseDao();
        bodyMeasurementDao = db.bodyMeasurementDao();
        progressPhotoDao = db.progressPhotoDao();
        userProfile = userDao.getUserProfile();
    }

    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }

    public void insertProfile(UserProfile profile) {
        databaseWriteExecutor.execute(() -> userDao.insertProfile(profile));
    }

    public void updateProfile(UserProfile profile) {
        databaseWriteExecutor.execute(() -> userDao.updateProfile(profile));
    }

    // Food methods
    public LiveData<List<FoodItem>> searchFood(String query) {
        return foodDao.searchFood("%" + query + "%");
    }

    public void logFood(LoggedFood loggedFood) {
        databaseWriteExecutor.execute(() -> foodDao.logFood(loggedFood));
    }

    public void deleteLoggedFood(LoggedFood loggedFood) {
        databaseWriteExecutor.execute(() -> foodDao.deleteLoggedFood(loggedFood));
    }

    public LiveData<List<LoggedFood>> getLoggedFoodsForDate(String date) {
        return foodDao.getLoggedFoodsForDate(date);
    }

    public LiveData<Float> getTotalCaloriesForDate(String date) {
        return foodDao.getTotalCaloriesForDate(date);
    }

    public LiveData<Float> getTotalProteinForDate(String date) {
        return foodDao.getTotalProteinForDate(date);
    }

    public LiveData<Float> getTotalCarbsForDate(String date) {
        return foodDao.getTotalCarbsForDate(date);
    }

    public LiveData<Float> getTotalFatForDate(String date) {
        return foodDao.getTotalFatForDate(date);
    }

    // Workout methods
    public LiveData<List<WorkoutPlan>> getAllPlans() {
        return workoutDao.getAllPlans();
    }

    public LiveData<List<WorkoutSession>> getAllSessions() {
        return workoutDao.getAllSessions();
    }

    public LiveData<List<PlanExerciseWithDetails>> getExercisesForPlan(long planId) {
        return workoutDao.getExercisesForPlan(planId);
    }

    public LiveData<List<WorkoutSet>> getSetsForSession(long sessionId) {
        return workoutDao.getSetsForSession(sessionId);
    }

    public WorkoutSet getLatestSetForExercise(String exerciseName) {
        return workoutDao.getLatestSetForExercise(exerciseName);
    }

    public LiveData<List<com.ps.qwertyfitness.data.local.entity.ExercisePR>> getPersonalRecords() {
        return workoutDao.getPersonalRecords();
    }

    public void insertPlan(WorkoutPlan plan, List<PlanExercise> exercises) {
        databaseWriteExecutor.execute(() -> {
            long planId = workoutDao.insertPlan(plan);
            for (PlanExercise pe : exercises) {
                pe.planId = planId;
                workoutDao.insertPlanExercise(pe);
            }
        });
    }

    public LiveData<List<Exercise>> searchExercises(String query) {
        return exerciseDao.searchExercises("%" + query + "%");
    }

    public void insertSession(WorkoutSession session, List<WorkoutSet> sets) {
        databaseWriteExecutor.execute(() -> {
            long sessionId = workoutDao.insertSession(session);
            for (WorkoutSet set : sets) {
                set.sessionId = sessionId;
                workoutDao.insertSet(set);
            }
        });
    }

    // Weight methods
    public LiveData<List<WeightEntry>> getAllWeightEntries() {
        return weightDao.getAllWeightEntries();
    }

    public LiveData<WeightEntry> getLatestWeight() {
        return weightDao.getLatestWeight();
    }

    public LiveData<List<WeightEntry>> getWeightEntriesSince(long timestamp) {
        return weightDao.getWeightEntriesSince(timestamp);
    }

    public void insertWeight(WeightEntry entry) {
        databaseWriteExecutor.execute(() -> weightDao.insert(entry));
    }

    // Measurement methods
    public LiveData<List<BodyMeasurement>> getAllMeasurements() {
        return bodyMeasurementDao.getAllMeasurements();
    }

    public LiveData<BodyMeasurement> getLatestMeasurementByPart(String partName) {
        return bodyMeasurementDao.getLatestMeasurementByPart(partName);
    }

    public void insertMeasurement(BodyMeasurement measurement) {
        databaseWriteExecutor.execute(() -> bodyMeasurementDao.insert(measurement));
    }

    // Photo methods
    public LiveData<List<ProgressPhoto>> getPhotosByCategory(String category) {
        return progressPhotoDao.getPhotosByCategory(category);
    }

    public void insertPhoto(ProgressPhoto photo) {
        databaseWriteExecutor.execute(() -> progressPhotoDao.insert(photo));
    }
}
