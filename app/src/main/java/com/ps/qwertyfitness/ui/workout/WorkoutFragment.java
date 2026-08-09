package com.ps.qwertyfitness.ui.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.ps.qwertyfitness.R;

import com.google.android.material.tabs.TabLayoutMediator;
import com.ps.qwertyfitness.databinding.FragmentWorkoutBinding;

public class WorkoutFragment extends Fragment {
    
    private FragmentWorkoutBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWorkoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        WorkoutPagerAdapter adapter = new WorkoutPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);
        
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Plans"); break;
                case 1: tab.setText("Exercises"); break;
                case 2: tab.setText("History"); break;
            }
        }).attach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
