package com.ps.qwertyfitness.ui.workout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.ps.qwertyfitness.ui.workout.tabs.ExercisesFragment;
import com.ps.qwertyfitness.ui.workout.tabs.HistoryFragment;
import com.ps.qwertyfitness.ui.workout.tabs.PlansFragment;

public class WorkoutPagerAdapter extends FragmentStateAdapter {

    public WorkoutPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new PlansFragment();
            case 1: return new ExercisesFragment();
            case 2: return new HistoryFragment();
            default: return new PlansFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}