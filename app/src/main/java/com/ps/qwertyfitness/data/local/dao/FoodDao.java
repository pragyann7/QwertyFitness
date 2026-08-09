package com.ps.qwertyfitness.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.ps.qwertyfitness.data.local.entity.FoodItem;
import com.ps.qwertyfitness.data.local.entity.LoggedFood;

import java.util.List;

@Dao
public interface FoodDao {
    @Query("SELECT * FROM food_items")
    List<FoodItem> getAllFoodItemsSync();

    @Query("SELECT * FROM food_items WHERE name LIKE :query")
    LiveData<List<FoodItem>> searchFood(String query);
    
    @Insert(onConflict = androidx.room.OnConflictStrategy.IGNORE)
    void insertFoodItem(FoodItem foodItem);
    
    @Insert
    void logFood(LoggedFood loggedFood);

    @androidx.room.Delete
    void deleteLoggedFood(LoggedFood loggedFood);
    
    @Query("SELECT * FROM logged_foods WHERE date = :date")
    LiveData<List<LoggedFood>> getLoggedFoodsForDate(String date);

    @Query("SELECT * FROM logged_foods WHERE date = :date")
    List<LoggedFood> getLoggedFoodsForDateSync(String date);

    @Query("SELECT SUM(calories) FROM logged_foods WHERE date = :date")
    LiveData<Float> getTotalCaloriesForDate(String date);

    @Query("SELECT SUM(protein) FROM logged_foods WHERE date = :date")
    LiveData<Float> getTotalProteinForDate(String date);

    @Query("SELECT SUM(carbs) FROM logged_foods WHERE date = :date")
    LiveData<Float> getTotalCarbsForDate(String date);

    @Query("SELECT SUM(fat) FROM logged_foods WHERE date = :date")
    LiveData<Float> getTotalFatForDate(String date);
}
