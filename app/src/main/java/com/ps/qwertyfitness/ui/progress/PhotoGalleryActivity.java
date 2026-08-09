package com.ps.qwertyfitness.ui.progress;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ps.qwertyfitness.databinding.ActivityPhotoGalleryBinding;

public class PhotoGalleryActivity extends AppCompatActivity {

    private ActivityPhotoGalleryBinding binding;
    private ProgressViewModel viewModel;
    private PhotoTimelineAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhotoGalleryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ProgressViewModel.class);

        binding.btnBack.setOnClickListener(v -> finish());

        adapter = new PhotoTimelineAdapter(photo -> {
            Intent intent = new Intent(this, ViewPhotoActivity.class);
            intent.putExtra("PHOTO_PATH", photo.imagePath);
            intent.putExtra("PHOTO_INFO", photo.category + " - " + photo.date);
            intent.putExtra("PHOTO_ID", photo.id);
            startActivity(intent);
        });

        binding.recyclerTimeline.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerTimeline.setAdapter(adapter);

        viewModel.getAllPhotos().observe(this, photos -> {
            adapter.setItems(photos);
        });
    }
}