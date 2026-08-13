package com.ps.qwertyfitness.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.ps.qwertyfitness.data.local.entity.WaterLog;

import java.util.List;

@Dao
public interface WaterDao {
    @Insert
    void insert(WaterLog log);

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_logs WHERE date = :date")
    LiveData<Integer> getTotalWaterForDate(String date);

    @Query("SELECT * FROM water_logs WHERE date = :date ORDER BY timestamp DESC")
    LiveData<List<WaterLog>> getLogsForDate(String date);
    
    @Query("DELETE FROM water_logs WHERE id = (SELECT id FROM water_logs WHERE date = :date ORDER BY timestamp DESC LIMIT 1)")
    void deleteLastLogForDate(String date);
}
