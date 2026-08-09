package com.ps.qwertyfitness.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.ps.qwertyfitness.data.local.entity.WeightEntry;

import java.util.List;

@Dao
public interface WeightDao {
    @Query("SELECT * FROM weight_entries ORDER BY timestamp ASC")
    LiveData<List<WeightEntry>> getAllWeightEntries();
    
    @Insert
    void insert(WeightEntry entry);
    
    @Query("SELECT * FROM weight_entries ORDER BY timestamp DESC LIMIT 1")
    LiveData<WeightEntry> getLatestWeight();

    @Query("SELECT * FROM weight_entries WHERE timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    LiveData<List<WeightEntry>> getWeightEntriesSince(long sinceTimestamp);
}
