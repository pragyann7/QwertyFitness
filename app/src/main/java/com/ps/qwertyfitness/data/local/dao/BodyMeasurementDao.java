package com.ps.qwertyfitness.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.ps.qwertyfitness.data.local.entity.BodyMeasurement;

import java.util.List;

@Dao
public interface BodyMeasurementDao {
    @Query("SELECT * FROM body_measurements ORDER BY timestamp DESC")
    LiveData<List<BodyMeasurement>> getAllMeasurements();

    @Query("SELECT * FROM body_measurements WHERE partName = :partName ORDER BY timestamp DESC")
    LiveData<List<BodyMeasurement>> getMeasurementsByPart(String partName);

    @Query("SELECT * FROM body_measurements WHERE partName = :partName ORDER BY timestamp DESC LIMIT 1")
    LiveData<BodyMeasurement> getLatestMeasurementByPart(String partName);

    @Insert
    void insert(BodyMeasurement measurement);
}