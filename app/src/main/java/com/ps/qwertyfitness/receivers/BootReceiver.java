package com.ps.qwertyfitness.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ps.qwertyfitness.data.local.entity.Reminder;
import com.ps.qwertyfitness.data.repository.FitnessRepository;
import com.ps.qwertyfitness.utils.ReminderManager;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            FitnessRepository repository = new FitnessRepository((android.app.Application) context.getApplicationContext());
            new Thread(() -> {
                List<Reminder> reminders = repository.getEnabledRemindersSync();
                for (Reminder reminder : reminders) {
                    ReminderManager.scheduleReminder(context, reminder);
                }
            }).start();
        }
    }
}
