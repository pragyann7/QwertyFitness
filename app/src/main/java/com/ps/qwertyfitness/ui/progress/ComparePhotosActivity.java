package com.ps.qwertyfitness.ui.progress;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.ps.qwertyfitness.data.local.entity.ProgressPhoto;
import com.ps.qwertyfitness.databinding.ActivityComparePhotosBinding;

import java.util.List;

public class ComparePhotosActivity extends AppCompatActivity {

    private ActivityComparePhotosBinding binding;
    private ProgressViewModel viewModel;
    private ProgressPhoto beforePhoto;
    private ProgressPhoto afterPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityComparePhotosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ProgressViewModel.class);

        binding.btnBack.setOnClickListener(v -> finish());

        binding.cardPhotoBefore.setOnClickListener(v -> showPhotoPicker(true));
        binding.cardPhotoAfter.setOnClickListener(v -> showPhotoPicker(false));

        binding.btnShare.setOnClickListener(v -> {
            // Placeholder for share functionality
        });
    }

    private void showPhotoPicker(boolean isBefore) {
        viewModel.getPhotosByCategory("Front").observe(this, photos -> {
            if (photos == null || photos.isEmpty()) {
                // Handle empty state
                return;
            }

            String[] dates = new String[photos.size()];
            for (int i = 0; i < photos.size(); i++) {
                dates[i] = photos.get(i).date;
            }

            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Select Photo")
                    .setItems(dates, (dialog, which) -> {
                        ProgressPhoto selected = photos.get(which);
                        if (isBefore) {
                            beforePhoto = selected;
                            updatePhotoUI(binding.imgBefore, binding.textPlaceholderBefore, binding.textDateBefore, selected);
                        } else {
                            afterPhoto = selected;
                            updatePhotoUI(binding.imgAfter, binding.textPlaceholderAfter, binding.textDateAfter, selected);
                        }
                    })
                    .show();
        });
    }

    private void updatePhotoUI(android.widget.ImageView imageView, View placeholder, android.widget.TextView dateText, ProgressPhoto photo) {
        if (photo.imagePath != null) {
            com.ps.qwertyfitness.utils.ImageUtils.loadThumbnail(imageView, photo.imagePath);
            placeholder.setVisibility(View.GONE);
            dateText.setText(photo.date);
        }
    }
}