package com.ps.qwertyfitness.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.ps.qwertyfitness.data.local.entity.Reminder;
import com.ps.qwertyfitness.receivers.ReminderReceiver;

import java.util.Calendar;

public class ReminderManager {

    public static void scheduleReminder(Context context, Reminder reminder) {
        if (!reminder.enabled) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("REMINDER_ID", reminder.id);
        intent.putExtra("REMINDER_TYPE", reminder.type);
        intent.putExtra("REMINDER_TITLE", reminder.title);
        intent.putExtra("REMINDER_TIME", reminder.time);
        intent.putExtra("REMINDER_REPEAT", reminder.repeatDays);
        intent.putExtra("REMINDER_INTERVAL", reminder.intervalMinutes);
        intent.putExtra("REMINDER_END_TIME", reminder.endTime);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) reminder.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerTime = calculateNextTriggerTime(reminder);

        try {
            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(
                    triggerTime,
                    pendingIntent
            );
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
        } catch (SecurityException e) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    public static void cancelReminder(Context context, Reminder reminder) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) reminder.id,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    public static void cancelSnooze(Context context, long id) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) id + 30000,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    public static long calculateNextTriggerTime(Reminder reminder) {
        if (reminder == null || reminder.time == null) return System.currentTimeMillis();
        
        long now = System.currentTimeMillis();
        Calendar nowCal = Calendar.getInstance();
        
        if (reminder.intervalMinutes > 0) {
            // Interval Logic
            Calendar startCal = getTimeCalendar(reminder.time);
            if (startCal == null) return now;

            if (isValidDay(reminder, nowCal)) {
                Calendar endCal = reminder.endTime != null ? getTimeCalendar(reminder.endTime) : null;
                
                if (now < startCal.getTimeInMillis()) {
                    return startCal.getTimeInMillis();
                }
                
                long intervalMillis = (long) reminder.intervalMinutes * 60 * 1000;
                long nextTrigger = startCal.getTimeInMillis() + (((now - startCal.getTimeInMillis()) / intervalMillis) + 1) * intervalMillis;
                
                if (endCal == null || nextTrigger <= endCal.getTimeInMillis()) {
                    return nextTrigger;
                }
            }
            // If today is not valid or we passed endTime, find next valid day's start
            return getNextValidStartTime(reminder, now);
        }

        // Time based (Once a day)
        long triggerToday = getTimeInMillis(reminder.time);
        if (triggerToday > now && isValidDay(reminder, nowCal)) {
            return triggerToday;
        } else {
            return getNextValidStartTime(reminder, now);
        }
    }

    private static long getNextValidStartTime(Reminder reminder, long fromTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(fromTime);
        
        String[] parts = reminder.time != null ? reminder.time.split(":") : new String[]{"0", "0"};
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);

        for (int i = 0; i < 8; i++) { // Check up to 7 days ahead
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            calendar.set(Calendar.HOUR_OF_DAY, h);
            calendar.set(Calendar.MINUTE, m);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            
            if (isValidDay(reminder, calendar)) break;
        }
        return calendar.getTimeInMillis();
    }

    private static Calendar getTimeCalendar(String timeStr) {
        if (timeStr == null || !timeStr.contains(":")) return Calendar.getInstance();
        try {
            String[] parts = timeStr.split(":");
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
            c.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            return c;
        } catch (Exception e) {
            return Calendar.getInstance();
        }
    }

    private static long getTimeInMillis(String timeStr) {
        return getTimeCalendar(timeStr).getTimeInMillis();
    }

    private static boolean isValidDay(Reminder reminder, Calendar calendar) {
        if (reminder.repeatDays == null || reminder.repeatDays.isEmpty()) return true;
        
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        // Map Sun(1) -> 7, Mon(2) -> 1 ... Sat(7) -> 6
        int normalizedDay = (dayOfWeek == Calendar.SUNDAY) ? 7 : dayOfWeek - 1;
        
        try {
            String[] days = reminder.repeatDays.split(",");
            for (String d : days) {
                if (d.trim().isEmpty()) continue;
                if (Integer.parseInt(d.trim()) == normalizedDay) return true;
            }
        } catch (Exception e) {
            return true; // Default to valid if parsing fails
        }
        return false;
    }
}
