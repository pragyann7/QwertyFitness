package com.ps.qwertyfitness.ui.progress;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ps.qwertyfitness.data.local.entity.ProgressPhoto;
import com.ps.qwertyfitness.databinding.ItemPhotoTimelineBinding;
import com.ps.qwertyfitness.utils.ImageUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PhotoTimelineAdapter extends RecyclerView.Adapter<PhotoTimelineAdapter.ViewHolder> {

    private final List<GroupedPhotos> groupedPhotos = new ArrayList<>();
    private final OnPhotoClickListener listener;

    public interface OnPhotoClickListener {
        void onPhotoClick(ProgressPhoto photo);
    }

    public static class GroupedPhotos {
        public String date;
        public Map<String, ProgressPhoto> categoryMap = new HashMap<>();

        public GroupedPhotos(String date) {
            this.date = date;
        }
    }

    public PhotoTimelineAdapter(OnPhotoClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ProgressPhoto> items) {
        groupedPhotos.clear();
        if (items != null) {
            Map<String, GroupedPhotos> tempMap = new HashMap<>();
            for (ProgressPhoto photo : items) {
                GroupedPhotos group = tempMap.get(photo.date);
                if (group == null) {
                    group = new GroupedPhotos(photo.date);
                    tempMap.put(photo.date, group);
                }
                group.categoryMap.put(photo.category, photo);
            }
            groupedPhotos.addAll(tempMap.values());
            // Sort by date descending (assuming YYYY-MM-DD format)
            groupedPhotos.sort((a, b) -> b.date.compareTo(a.date));
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemPhotoTimelineBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(groupedPhotos.get(position));
    }

    @Override
    public int getItemCount() {
        return groupedPhotos.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPhotoTimelineBinding binding;

        ViewHolder(ItemPhotoTimelineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(GroupedPhotos group) {
            binding.textDate.setText(group.date.toUpperCase(Locale.getDefault()));
            
            setupImage(binding.imgFront, group.categoryMap.get("Front"));
            setupImage(binding.imgSide, group.categoryMap.get("Side"));
            setupImage(binding.imgBack, group.categoryMap.get("Back"));
        }

        private void setupImage(android.widget.ImageView iv, ProgressPhoto photo) {
            if (photo != null) {
                ImageUtils.loadThumbnail(iv, photo.imagePath);
                iv.setOnClickListener(v -> listener.onPhotoClick(photo));
                iv.setAlpha(1.0f);
            } else {
                iv.setImageBitmap(null);
                iv.setOnClickListener(null);
                iv.setAlpha(0.2f);
            }
        }
    }
}