package com.ps.qwertyfitness.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.ps.qwertyfitness.R;
import com.ps.qwertyfitness.ui.onboarding.OnboardingActivity;
import com.ps.qwertyfitness.data.local.AppDatabase;
import com.ps.qwertyfitness.data.local.dao.FoodDao;
import com.ps.qwertyfitness.data.local.dao.UserDao;
import com.ps.qwertyfitness.data.local.dao.WaterDao;
import com.ps.qwertyfitness.data.local.dao.WorkoutDao;
import com.ps.qwertyfitness.data.local.entity.UserProfile;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class DailyProgressWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_daily_progress);

        // Target OnboardingActivity to ensure Splash Screen shows and profile is checked
        Intent intent = new Intent(context, OnboardingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);

        // Shortcut to Start Workout
        Intent workoutIntent = new Intent(context, OnboardingActivity.class);
        workoutIntent.putExtra("ACTION", "START_WORKOUT");
        PendingIntent workoutPI = PendingIntent.getActivity(context, 1, workoutIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_btn_start, workoutPI);

        // Fetch Data in background
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(context);
            UserDao userDao = db.userDao();
            FoodDao foodDao = db.foodDao();
            WaterDao waterDao = db.waterDao();
            WorkoutDao workoutDao = db.workoutDao();

            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            UserProfile profile = userDao.getUserProfileSync();
            
            if (profile != null) {
                // 1. Streak Calculation (similar logic to FitnessRepository)
                List<String> dates = workoutDao.getDistinctWorkoutDatesSync();
                int streak = calculateStreak(dates);
                views.setTextViewText(R.id.widget_text_streak_count, String.valueOf(streak));

                // 2. Nutrition Data
                float calories = getSum(foodDao.getTotalCaloriesSync(today));
                float protein = getSum(foodDao.getTotalProteinSync(today));
                
                views.setTextViewText(R.id.widget_text_calories, (int)calories + "/" + profile.dailyCalorieTarget);
                int calProgress = profile.dailyCalorieTarget > 0 ? (int)((calories / profile.dailyCalorieTarget) * 100) : 0;
                views.setProgressBar(R.id.widget_progress_calories, 100, Math.min(calProgress, 100), false);

                // 2.1 Protein Data
                views.setTextViewText(R.id.widget_text_protein, (int)protein + "/" + profile.proteinTarget + "g");
                int proteinProgress = profile.proteinTarget > 0 ? (int)((protein / profile.proteinTarget) * 100) : 0;
                views.setProgressBar(R.id.widget_progress_protein, 100, Math.min(proteinProgress, 100), false);

                // 3. Water Data
                Integer water = waterDao.getTotalWaterForDateSync(today);
                int waterVal = water != null ? water : 0;
                views.setTextViewText(R.id.widget_text_water, waterVal + "/" + profile.waterTarget);
                int waterProgress = profile.waterTarget > 0 ? (int)(((float)waterVal / profile.waterTarget) * 100) : 0;
                views.setProgressBar(R.id.widget_progress_water, 100, Math.min(waterProgress, 100), false);
            } else {
                // Clear widget if app was reset / profile is missing
                views.setTextViewText(R.id.widget_text_streak_count, "0");
                views.setTextViewText(R.id.widget_text_calories, "0/0");
                views.setTextViewText(R.id.widget_text_protein, "0/0");
                views.setTextViewText(R.id.widget_text_water, "0/0");
                views.setProgressBar(R.id.widget_progress_calories, 100, 0, false);
                views.setProgressBar(R.id.widget_progress_protein, 100, 0, false);
                views.setProgressBar(R.id.widget_progress_water, 100, 0, false);
            }
            appWidgetManager.updateAppWidget(appWidgetId, views);
        });
    }

    private static float getSum(Float val) {
        return val != null ? val : 0f;
    }

    private static int calculateStreak(List<String> dates) {
        if (dates == null || dates.isEmpty()) return 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        String today = sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = sdf.format(cal.getTime());

        if (!dates.contains(today) && !dates.contains(yesterday)) return 0;

        int streak = 0;
        cal = Calendar.getInstance();
        if (!dates.contains(today)) cal.add(Calendar.DAY_OF_YEAR, -1);
        while (dates.contains(sdf.format(cal.getTime()))) {
            streak++;
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
        return streak;
    }

    public static void updateAllWidgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, DailyProgressWidget.class));
        for (int id : ids) {
            updateAppWidget(context, appWidgetManager, id);
        }
    }
}
