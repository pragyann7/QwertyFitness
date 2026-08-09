package com.ps.qwertyfitness.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.ps.qwertyfitness.data.local.entity.ProgressPhoto;

import java.util.List;

@Dao
public interface ProgressPhotoDao {
    @Query("SELECT * FROM progress_photos WHERE category = :category ORDER BY timestamp DESC")
    LiveData<List<ProgressPhoto>> getPhotosByCategory(String category);

    @Query("SELECT * FROM progress_photos ORDER BY timestamp DESC")
    LiveData<List<ProgressPhoto>> getAllPhotos();

    @Insert
    void insert(ProgressPhoto photo);

    @Delete
    void delete(ProgressPhoto photo);
}