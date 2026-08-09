package com.ps.qwertyfitness.ui.progress;

import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ps.qwertyfitness.data.local.entity.ProgressPhoto;
import com.ps.qwertyfitness.databinding.ActivityViewPhotoBinding;

public class ViewPhotoActivity extends AppCompatActivity {

    private ActivityViewPhotoBinding binding;
    private ProgressViewModel viewModel;
    private long photoId;
    private String photoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewPhotoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ProgressViewModel.class);

        photoId = getIntent().getLongExtra("PHOTO_ID", -1);
        photoPath = getIntent().getStringExtra("PHOTO_PATH");
        String photoInfo = getIntent().getStringExtra("PHOTO_INFO");

        if (photoPath != null) {
            com.ps.qwertyfitness.utils.ImageUtils.loadFullPhoto(binding.imgFullScreen, photoPath);
        }
        binding.textPhotoInfo.setText(photoInfo);

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnDelete.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Delete Photo")
                    .setMessage("Are you sure you want to permanently delete this photo?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        ProgressPhoto photo = new ProgressPhoto();
                        photo.id = photoId;
                        photo.imagePath = photoPath;
                        viewModel.deletePhoto(photo);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}