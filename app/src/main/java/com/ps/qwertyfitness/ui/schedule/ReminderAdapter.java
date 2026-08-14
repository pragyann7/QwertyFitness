package com.ps.qwertyfitness.ui.schedule;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ps.qwertyfitness.data.local.entity.Reminder;
import com.ps.qwertyfitness.databinding.ItemReminderBinding;

public class ReminderAdapter extends ListAdapter<Reminder, ReminderAdapter.ViewHolder> {

    private final OnReminderChangeListener listener;

    public interface OnReminderChangeListener {
        void onToggle(Reminder reminder, boolean isEnabled);
        void onDelete(Reminder reminder);
        void onEdit(Reminder reminder);
    }

    public ReminderAdapter(OnReminderChangeListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Reminder> DIFF_CALLBACK = new DiffUtil.ItemCallback<Reminder>() {
        @Override
        public boolean areItemsTheSame(@NonNull Reminder oldItem, @NonNull Reminder newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Reminder oldItem, @NonNull Reminder newItem) {
            return oldItem.enabled == newItem.enabled && 
                   oldItem.time.equals(newItem.time) && 
                   oldItem.title.equals(newItem.title);
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemReminderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemReminderBinding binding;

        ViewHolder(ItemReminderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Reminder reminder, OnReminderChangeListener listener) {
            binding.textReminderTitle.setText(reminder.title);
            binding.textReminderTime.setText(reminder.time);
            binding.switchReminder.setChecked(reminder.enabled);

            binding.switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
                listener.onToggle(reminder, isChecked);
            });

            binding.getRoot().setOnClickListener(v -> {
                listener.onEdit(reminder);
            });

            binding.getRoot().setOnLongClickListener(v -> {
                listener.onDelete(reminder);
                return true;
            });
        }
    }
}
