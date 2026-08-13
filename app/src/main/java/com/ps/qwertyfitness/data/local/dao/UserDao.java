package com.ps.qwertyfitness.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ps.qwertyfitness.data.local.entity.UserProfile;

@Dao
public interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    LiveData<UserProfile> getUserProfile();

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    UserProfile getUserProfileSync();
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProfile(UserProfile profile);
    
    @Update
    void updateProfile(UserProfile profile);
}