package com.ps.qwertyfitness.receivers;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.ps.qwertyfitness.MainActivity;
import com.ps.qwertyfitness.R;
import com.ps.qwertyfitness.data.local.entity.LoggedFood;
import com.ps.qwertyfitness.data.local.entity.Reminder;
import com.ps.qwertyfitness.data.repository.FitnessRepository;
import com.ps.qwertyfitness.utils.ReminderManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReminderReceiver extends BroadcastReceiver {

    public static final String ACTION_MARK_DONE = "com.ps.qwertyfitness.ACTION_MARK_DONE";
    public static final String ACTION_SNOOZE = "com.ps.qwertyfitness.ACTION_SNOOZE";
    private static final String CHANNEL_ID = "REMINDER_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        long reminderId = intent.getLongExtra("REMINDER_ID", -1);
        String type = intent.getStringExtra("REMINDER_TYPE");
        String title = intent.getStringExtra("REMINDER_TITLE");
        String time = intent.getStringExtra("REMINDER_TIME");
        String repeat = intent.getStringExtra("REMINDER_REPEAT");
        int interval = intent.getIntExtra("REMINDER_INTERVAL", 0);
        String endTime = intent.getStringExtra("REMINDER_END_TIME");

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (ACTION_MARK_DONE.equals(intent.getAction())) {
            handleMarkDone(context, title, type);
            nm.cancel((int) reminderId);
            return;
        }

        if (ACTION_SNOOZE.equals(intent.getAction())) {
            handleSnooze(context, reminderId, title, type, time, repeat, interval, endTime);
            nm.cancel((int) reminderId);
            android.widget.Toast.makeText(context, "Snoozed for 1 minute", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // Reschedule
        rescheduleNext(context, reminderId, title, type, time, repeat, interval, endTime);

        showNotification(context, (int) reminderId, title, "Time for your " + type.toLowerCase() + "!", type, time, repeat, interval, endTime);
    }

    private void handleSnooze(Context context, long id, String title, String type, String time, String repeat, int interval, String endTime) {
        int snoozeMinutes = 1; // 1 minute for testing
        long snoozeTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000);
        
        FitnessRepository repository = new FitnessRepository((android.app.Application) context.getApplicationContext());
        new Thread(() -> {
            repository.updateSnoozeTime(id, snoozeTime);
        }).start();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("REMINDER_ID", id);
        intent.putExtra("REMINDER_TYPE", type);
        intent.putExtra("REMINDER_TITLE", title);
        intent.putExtra("REMINDER_TIME", time);
        intent.putExtra("REMINDER_REPEAT", repeat);
        intent.putExtra("REMINDER_INTERVAL", interval);
        intent.putExtra("REMINDER_END_TIME", endTime);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) id + 30000, // Different request code for snooze trigger
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(snoozeTime, pendingIntent);
            alarmManager.setAlarmClock(info, pendingIntent);
        } catch (SecurityException e) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent);
        }
    }

    private void rescheduleNext(Context context, long id, String title, String type, String time, String repeat, int interval, String endTime) {
        Reminder r = new Reminder();
        r.id = id;
        r.title = title;
        r.type = type;
        r.time = time;
        r.repeatDays = repeat;
        r.intervalMinutes = interval;
        r.endTime = endTime;
        r.enabled = true;
        ReminderManager.scheduleReminder(context, r);
    }

    private void handleMarkDone(Context context, String title, String type) {
        FitnessRepository repository = new FitnessRepository((android.app.Application) context.getApplicationContext());
        if ("MEAL".equals(type) || "WATER".equals(type) || "WORKOUT".equals(type) || "WEIGHT".equals(type)) {
            LoggedFood food = new LoggedFood();
            food.foodName = title;
            food.mealType = title; 
            food.date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            food.calories = 0; 
            repository.logFood(food);
        }
    }

    private void showNotification(Context context, int id, String title, String message, String type, String time, String repeat, int interval, String endTime) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        android.net.Uri soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM);
        if (soundUri == null) {
            soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            android.media.AudioAttributes audioAttributes = new android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                    .build();
            channel.setSound(soundUri, audioAttributes);
            notificationManager.createNotificationChannel(channel);
        }

        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                id,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent doneIntent = new Intent(context, ReminderReceiver.class);
        doneIntent.setAction(ACTION_MARK_DONE);
        doneIntent.putExtra("REMINDER_ID", (long) id);
        doneIntent.putExtra("REMINDER_TITLE", title);
        doneIntent.putExtra("REMINDER_TYPE", type);
        PendingIntent donePendingIntent = PendingIntent.getBroadcast(
                context,
                id + 10000,
                doneIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent snoozeIntent = new Intent(context, ReminderReceiver.class);
        snoozeIntent.setAction(ACTION_SNOOZE);
        snoozeIntent.putExtra("REMINDER_ID", (long) id);
        snoozeIntent.putExtra("REMINDER_TITLE", title);
        snoozeIntent.putExtra("REMINDER_TYPE", type);
        snoozeIntent.putExtra("REMINDER_TIME", time);
        snoozeIntent.putExtra("REMINDER_REPEAT", repeat);
        snoozeIntent.putExtra("REMINDER_INTERVAL", interval);
        snoozeIntent.putExtra("REMINDER_END_TIME", endTime);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                id + 20000,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_workout)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSound(soundUri)
                .setAutoCancel(true)
                .setFullScreenIntent(contentIntent, true) 
                .setContentIntent(contentIntent)
                .addAction(R.drawable.ic_home, "MARK AS DONE", donePendingIntent);

        if (!"WATER".equals(type)) {
            builder.addAction(R.drawable.ic_home, "LATER", snoozePendingIntent);
        }

        notificationManager.notify(id, builder.build());
    }
}
