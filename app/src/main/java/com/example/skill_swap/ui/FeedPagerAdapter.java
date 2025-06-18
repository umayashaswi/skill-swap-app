package com.example.skill_swap.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class FeedPagerAdapter extends FragmentStateAdapter {

    public FeedPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new PublicFeedFragment();
        } else {
            return new ConnectionsFeedFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
