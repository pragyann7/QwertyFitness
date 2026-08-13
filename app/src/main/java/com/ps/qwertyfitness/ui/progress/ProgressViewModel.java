package com.ps.qwertyfitness.ui.progress;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.ps.qwertyfitness.data.local.entity.BodyMeasurement;
import com.ps.qwertyfitness.data.local.entity.ExerciseProgressPoint;
import com.ps.qwertyfitness.data.local.entity.ExercisePR;
import com.ps.qwertyfitness.data.local.entity.ProgressPhoto;
import com.ps.qwertyfitness.data.local.entity.UserProfile;
import com.ps.qwertyfitness.data.local.entity.WeightEntry;
import com.ps.qwertyfitness.data.local.entity.WorkoutSetWithDate;
import com.ps.qwertyfitness.data.repository.FitnessRepository;

import java.util.List;

public class ProgressViewModel extends AndroidViewModel {
    private final FitnessRepository repository;
    private final MutableLiveData<String> graphFilter = new MutableLiveData<>("ALL");
    private final LiveData<List<WeightEntry>> filteredGraphData;

    public ProgressViewModel(@NonNull Application application) {
        super(application);
        repository = new FitnessRepository(application);
        
        filteredGraphData = Transformations.switchMap(graphFilter, filter -> {
            long sinceTimestamp;
            long now = System.currentTimeMillis();
            if ("7D".equals(filter)) {
                sinceTimestamp = now - (7L * 24 * 60 * 60 * 1000);
            } else if ("30D".equals(filter)) {
                sinceTimestamp = now - (30L * 24 * 60 * 60 * 1000);
            } else if ("3M".equals(filter)) {
                sinceTimestamp = now - (90L * 24 * 60 * 60 * 1000);
            } else if ("6M".equals(filter)) {
                sinceTimestamp = now - (180L * 24 * 60 * 60 * 1000);
            } else if ("1Y".equals(filter)) {
                sinceTimestamp = now - (365L * 24 * 60 * 60 * 1000);
            } else {
                sinceTimestamp = 0;
            }
            return repository.getWeightEntriesSince(sinceTimestamp);
        });
    }

    public LiveData<List<WeightEntry>> getAllWeightEntries() {
        return repository.getAllWeightEntries();
    }

    public LiveData<WeightEntry> getLatestWeight() {
        return repository.getLatestWeight();
    }

    public LiveData<UserProfile> getUserProfile() {
        return repository.getUserProfile();
    }

    public void setGraphFilter(String filter) {
        graphFilter.setValue(filter);
    }

    public LiveData<List<WeightEntry>> getFilteredGraphData() {
        return filteredGraphData;
    }

    public LiveData<List<ExercisePR>> getPersonalRecords() {
        return repository.getPersonalRecords();
    }

    public void logWeight(float weight, String date) {
        logWeightWithTimestamp(weight, date, System.currentTimeMillis());
    }

    public void logWeightWithTimestamp(float weight, String date, long timestamp) {
        WeightEntry entry = new WeightEntry();
        entry.weight = weight;
        entry.date = date;
        entry.timestamp = timestamp;
        repository.insertWeight(entry);
    }

    public void deleteWeight(WeightEntry entry) {
        repository.deleteWeight(entry);
    }

    public LiveData<BodyMeasurement> getLatestMeasurement(String partName) {
        return repository.getLatestMeasurementByPart(partName);
    }

    public void logMeasurement(String partName, float value, String unit, String date) {
        BodyMeasurement measurement = new BodyMeasurement();
        measurement.partName = partName;
        measurement.value = value;
        measurement.unit = unit;
        measurement.date = date;
        measurement.timestamp = System.currentTimeMillis();
        repository.insertMeasurement(measurement);
    }

    public LiveData<List<BodyMeasurement>> getMeasurementHistory(String partName) {
        return repository.getMeasurementHistory(partName);
    }

    public LiveData<List<WorkoutSetWithDate>> getExerciseHistory(String exerciseName) {
        return repository.getExerciseHistory(exerciseName);
    }

    public LiveData<List<ExerciseProgressPoint>> getExerciseProgressPoints(String exerciseName) {
        return repository.getExerciseProgressPoints(exerciseName);
    }

    public LiveData<com.ps.qwertyfitness.data.local.entity.Exercise> getExerciseDetails(String exerciseName) {
        MutableLiveData<com.ps.qwertyfitness.data.local.entity.Exercise> data = new MutableLiveData<>();
        new Thread(() -> {
            data.postValue(repository.getExerciseByNameSync(exerciseName));
        }).start();
        return data;
    }

    public LiveData<List<ProgressPhoto>> getPhotosByCategory(String category) {
        return repository.getPhotosByCategory(category);
    }

    public LiveData<ProgressPhoto> getLatestPhotoByCategory(String category) {
        return repository.getLatestPhotoByCategory(category);
    }

    public LiveData<List<ProgressPhoto>> getAllPhotos() {
        return repository.getAllPhotos();
    }

    public void addProgressPhoto(String path, String category, String date) {
        ProgressPhoto photo = new ProgressPhoto();
        photo.imagePath = path;
        photo.category = category;
        photo.date = date;
        photo.timestamp = System.currentTimeMillis();
        repository.insertPhoto(photo);
    }

    public void deletePhoto(ProgressPhoto photo) {
        if (photo.imagePath != null) {
            com.ps.qwertyfitness.utils.ImageUtils.deleteImage(photo.imagePath);
        }
        repository.deletePhoto(photo);
    }
}