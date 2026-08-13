package com.ps.qwertyfitness.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.ps.qwertyfitness.data.local.entity.Reminder;

import java.util.List;

@Dao
public interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY time ASC")
    LiveData<List<Reminder>> getAllReminders();

    @Query("SELECT * FROM reminders WHERE enabled = 1")
    List<Reminder> getEnabledRemindersSync();

    @Query("SELECT * FROM reminders WHERE id = :id")
    Reminder getReminderById(long id);

    @Query("SELECT * FROM reminders WHERE planId = :planId LIMIT 1")
    Reminder getReminderByPlanId(long planId);

    @Insert
    long insert(Reminder reminder);

    @Update
    void update(Reminder reminder);

    @Query("UPDATE reminders SET snoozeUntil = :snoozeTime WHERE id = :id")
    void updateSnoozeTime(long id, long snoozeTime);

    @Query("DELETE FROM reminders WHERE planId = :planId")
    void deleteByPlanId(long planId);

    @Delete
    void delete(Reminder reminder);
}
