package com.ps.qwertyfitness.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.ps.qwertyfitness.data.local.AppDatabase;
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
import com.ps.qwertyfitness.data.local.entity.ExerciseProgressPoint;
import com.ps.qwertyfitness.data.local.entity.FoodItem;
import com.ps.qwertyfitness.data.local.entity.LoggedFood;
import com.ps.qwertyfitness.data.local.entity.PlanExercise;
import com.ps.qwertyfitness.data.local.entity.PlanExerciseWithDetails;
import com.ps.qwertyfitness.data.local.entity.ProgressPhoto;
import com.ps.qwertyfitness.data.local.entity.Reminder;
import com.ps.qwertyfitness.data.local.entity.UserProfile;
import com.ps.qwertyfitness.data.local.entity.WaterLog;
import com.ps.qwertyfitness.data.local.entity.WeightEntry;
import com.ps.qwertyfitness.data.local.entity.WorkoutPlan;
import com.ps.qwertyfitness.data.local.entity.WorkoutSession;
import com.ps.qwertyfitness.data.local.entity.WorkoutSet;
import com.ps.qwertyfitness.data.local.entity.WorkoutSetWithDate;

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
    private final ReminderDao reminderDao;
    private final WaterDao waterDao;
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
        reminderDao = db.reminderDao();
        waterDao = db.waterDao();
        userProfile = userDao.getUserProfile();
    }

    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }

    public void insertProfile(UserProfile profile) {
        databaseWriteExecutor.execute(() -> {
            userDao.insertProfile(profile);
            
            // Automatically log the initial weight from profile into weight history
            WeightEntry entry = new WeightEntry();
            entry.weight = profile.weight;
            entry.date = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
            entry.timestamp = System.currentTimeMillis();
            weightDao.insert(entry);
        });
    }

    public void updateProfile(UserProfile profile) {
        databaseWriteExecutor.execute(() -> {
            userDao.updateProfile(profile);
            
            // Check if we should log a new weight entry on profile update
            // (Optional: only if weight changed significantly or it's a new day)
            WeightEntry entry = new WeightEntry();
            entry.weight = profile.weight;
            entry.date = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
            entry.timestamp = System.currentTimeMillis();
            weightDao.insert(entry);
        });
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

    public LiveData<List<LoggedFood>> getLoggedFoodsOnlyForDate(String date) {
        return foodDao.getLoggedFoodsOnlyForDate(date);
    }

    public List<LoggedFood> getLoggedFoodsForDateSync(String date) {
        return foodDao.getLoggedFoodsForDateSync(date);
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

    public LiveData<java.util.Set<String>> getWorkoutDatesForRange() {
        androidx.lifecycle.MutableLiveData<java.util.Set<String>> data = new androidx.lifecycle.MutableLiveData<>(new java.util.HashSet<>());
        databaseWriteExecutor.execute(() -> {
            List<String> dates = workoutDao.getDistinctWorkoutDatesSync();
            if (dates != null) {
                data.postValue(new java.util.HashSet<>(dates));
            }
        });
        return data;
    }

    public LiveData<Integer> getActiveStreak() {
        androidx.lifecycle.MutableLiveData<Integer> streakData = new androidx.lifecycle.MutableLiveData<>(0);
        databaseWriteExecutor.execute(() -> {
            List<String> dates = workoutDao.getDistinctWorkoutDatesSync();
            if (dates == null || dates.isEmpty()) {
                streakData.postValue(0);
                return;
            }

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Calendar cal = java.util.Calendar.getInstance();
            String today = sdf.format(cal.getTime());
            
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1);
            String yesterday = sdf.format(cal.getTime());

            int streak = 0;
            boolean hasWorkoutToday = dates.contains(today);
            boolean hasWorkoutYesterday = dates.contains(yesterday);

            if (!hasWorkoutToday && !hasWorkoutYesterday) {
                streakData.postValue(0);
                return;
            }

            // Start from today or yesterday
            cal = java.util.Calendar.getInstance();
            if (!hasWorkoutToday) {
                cal.add(java.util.Calendar.DAY_OF_YEAR, -1);
            }

            // Count backwards
            while (dates.contains(sdf.format(cal.getTime()))) {
                streak++;
                cal.add(java.util.Calendar.DAY_OF_YEAR, -1);
            }

            streakData.postValue(streak);
        });
        return streakData;
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

    public float getMaxWeightForExercise(String exerciseName) {
        return workoutDao.getMaxWeightForExerciseSync(exerciseName);
    }

    public int getMaxRepsForExercise(String exerciseName) {
        return workoutDao.getMaxRepsForExerciseSync(exerciseName);
    }

    public LiveData<List<com.ps.qwertyfitness.data.local.entity.ExercisePR>> getPersonalRecords() {
        return workoutDao.getPersonalRecords();
    }

    public LiveData<List<WorkoutSetWithDate>> getExerciseHistory(String exerciseName) {
        return workoutDao.getExerciseHistoryWithDate(exerciseName);
    }

    public LiveData<List<ExerciseProgressPoint>> getExerciseProgressPoints(String exerciseName) {
        return workoutDao.getExerciseProgressPoints(exerciseName);
    }

    public void insertPlan(WorkoutPlan plan, List<PlanExercise> exercises, Reminder reminder, Runnable onDone) {
        databaseWriteExecutor.execute(() -> {
            long planId = workoutDao.insertPlan(plan);
            for (PlanExercise pe : exercises) {
                pe.planId = planId;
                workoutDao.insertPlanExercise(pe);
            }
            
            if (reminder != null) {
                reminder.planId = planId;
                long reminderId = reminderDao.insert(reminder);
                reminder.id = reminderId;
            }
            
            if (onDone != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(onDone);
            }
        });
    }

    public void updatePlan(WorkoutPlan plan, List<PlanExercise> exercises, Reminder reminder, Runnable onDone) {
        databaseWriteExecutor.execute(() -> {
            workoutDao.updatePlan(plan);
            workoutDao.deletePlanExercisesByPlanId(plan.id);
            for (PlanExercise pe : exercises) {
                pe.planId = plan.id;
                workoutDao.insertPlanExercise(pe);
            }
            
            if (reminder != null) {
                reminder.planId = plan.id;
                Reminder existing = reminderDao.getReminderByPlanId(plan.id);
                if (existing != null) {
                    reminder.id = existing.id;
                    reminderDao.update(reminder);
                } else {
                    long id = reminderDao.insert(reminder);
                    reminder.id = id;
                }
            } else {
                // If reminder is null, check if there's an existing one to delete
                reminderDao.deleteByPlanId(plan.id);
            }
            
            if (onDone != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(onDone);
            }
        });
    }

    public void deletePlans(java.util.Set<Long> planIds) {
        databaseWriteExecutor.execute(() -> {
            for (Long id : planIds) {
                workoutDao.deletePlanById(id);
                workoutDao.deletePlanExercisesByPlanId(id);
                reminderDao.deleteByPlanId(id);
            }
        });
    }

    public LiveData<List<Exercise>> searchExercises(String query) {
        return exerciseDao.searchExercises("%" + query + "%");
    }

    public LiveData<List<Exercise>> searchExercisesFiltered(String query, String muscleGroup, String equipment) {
        return exerciseDao.searchExercisesFiltered("%" + query + "%", muscleGroup, equipment);
    }

    public void insertSession(WorkoutSession session, List<WorkoutSet> sets, Runnable onComplete) {
        databaseWriteExecutor.execute(() -> {
            long sessionId = workoutDao.insertSession(session);
            session.id = sessionId; // Update the session object with the new ID
            for (WorkoutSet set : sets) {
                set.sessionId = sessionId;
                workoutDao.insertSet(set);
            }
            if (onComplete != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(onComplete);
            }
        });
    }

    public void updateSession(WorkoutSession session) {
        databaseWriteExecutor.execute(() -> workoutDao.updateSession(session));
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
        databaseWriteExecutor.execute(() -> {
            weightDao.insert(entry);
            
            // Always sync profile with the absolute latest weight in history
            WeightEntry latest = weightDao.getLatestWeightSync();
            if (latest != null) {
                UserProfile profile = userDao.getUserProfileSync();
                if (profile != null) {
                    profile.weight = latest.weight;
                    com.ps.qwertyfitness.utils.FitnessCalculator.calculateTargets(profile);
                    userDao.updateProfile(profile);
                }
            }
        });
    }

    public void deleteWeight(WeightEntry entry) {
        databaseWriteExecutor.execute(() -> {
            weightDao.delete(entry);
            
            // Sync profile with the new latest weight after deletion
            WeightEntry newLatest = weightDao.getLatestWeightSync();
            if (newLatest != null) {
                UserProfile profile = userDao.getUserProfileSync();
                if (profile != null) {
                    profile.weight = newLatest.weight;
                    com.ps.qwertyfitness.utils.FitnessCalculator.calculateTargets(profile);
                    userDao.updateProfile(profile);
                }
            }
        });
    }

    // Measurement methods
    public LiveData<List<BodyMeasurement>> getAllMeasurements() {
        return bodyMeasurementDao.getAllMeasurements();
    }

    public LiveData<BodyMeasurement> getLatestMeasurementByPart(String partName) {
        return bodyMeasurementDao.getLatestMeasurementByPart(partName);
    }

    public LiveData<List<BodyMeasurement>> getMeasurementHistory(String partName) {
        return bodyMeasurementDao.getMeasurementsByPart(partName);
    }

    public void insertMeasurement(BodyMeasurement measurement) {
        databaseWriteExecutor.execute(() -> bodyMeasurementDao.insert(measurement));
    }

    // Photo methods
    public LiveData<List<ProgressPhoto>> getPhotosByCategory(String category) {
        return progressPhotoDao.getPhotosByCategory(category);
    }

    public LiveData<ProgressPhoto> getLatestPhotoByCategory(String category) {
        return progressPhotoDao.getLatestPhotoByCategory(category);
    }

    public LiveData<List<ProgressPhoto>> getAllPhotos() {
        return progressPhotoDao.getAllPhotos();
    }

    public void insertPhoto(ProgressPhoto photo) {
        databaseWriteExecutor.execute(() -> progressPhotoDao.insert(photo));
    }

    public void deletePhoto(ProgressPhoto photo) {
        databaseWriteExecutor.execute(() -> progressPhotoDao.delete(photo));
    }

    // Reminder methods
    public LiveData<List<Reminder>> getAllReminders() {
        return reminderDao.getAllReminders();
    }

    public List<Reminder> getEnabledRemindersSync() {
        return reminderDao.getEnabledRemindersSync();
    }

    public Reminder getReminderByIdSync(long id) {
        return reminderDao.getReminderById(id);
    }

    public void insertReminder(Reminder reminder, Runnable onDone) {
        databaseWriteExecutor.execute(() -> {
            long id = reminderDao.insert(reminder);
            reminder.id = id;
            if (onDone != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(onDone);
            }
        });
    }

    public void updateReminder(Reminder reminder) {
        databaseWriteExecutor.execute(() -> reminderDao.update(reminder));
    }

    public void updateSnoozeTime(long id, long snoozeTime) {
        databaseWriteExecutor.execute(() -> reminderDao.updateSnoozeTime(id, snoozeTime));
    }

    // Water methods
    public LiveData<Integer> getTotalWaterForDate(String date) {
        return waterDao.getTotalWaterForDate(date);
    }

    public void logWater(WaterLog log) {
        databaseWriteExecutor.execute(() -> waterDao.insert(log));
    }

    public void deleteLastWaterLog(String date) {
        databaseWriteExecutor.execute(() -> waterDao.deleteLastLogForDate(date));
    }

    public void logActivityCompletion(String activityName) {
        logActivityCompletion(activityName, new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date()));
    }

    public void logActivityCompletion(String activityName, String date) {
        databaseWriteExecutor.execute(() -> {
            LoggedFood activity = new LoggedFood();
            activity.foodName = activityName;
            activity.mealType = activityName;
            activity.date = date;
            activity.calories = 0;
            activity.isActivity = true;
            foodDao.logFood(activity);
        });
    }

    public void deleteLoggedActivity(String activityName, String date) {
        databaseWriteExecutor.execute(() -> {
            List<LoggedFood> logs = foodDao.getLoggedFoodsForDateSync(date);
            if (logs != null) {
                for (LoggedFood log : logs) {
                    if (activityName.equalsIgnoreCase(log.mealType) && log.isActivity) {
                        foodDao.deleteLoggedFood(log);
                        break;
                    }
                }
            }
        });
    }

    public void deleteReminder(Reminder reminder) {
        databaseWriteExecutor.execute(() -> reminderDao.delete(reminder));
    }

    public Exercise getExerciseByNameSync(String name) {
        return exerciseDao.getExerciseByNameSync(name);
    }

    public void saveWorkoutReminder(Reminder reminder, Runnable onDone) {
        databaseWriteExecutor.execute(() -> {
            if (reminder.planId != null) {
                Reminder existing = reminderDao.getReminderByPlanId(reminder.planId);
                if (existing != null) {
                    reminder.id = existing.id;
                    reminderDao.update(reminder);
                } else {
                    long id = reminderDao.insert(reminder);
                    reminder.id = id;
                }
            } else {
                long id = reminderDao.insert(reminder);
                reminder.id = id;
            }
            
            if (onDone != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(onDone);
            }
        });
    }
}
