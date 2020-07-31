package android.example.vehiclemaintenancetracker.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import android.example.vehiclemaintenancetracker.DashboardFragment;
import android.example.vehiclemaintenancetracker.HistoryFragment;
import android.example.vehiclemaintenancetracker.R;

import org.jetbrains.annotations.NotNull;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_dashboard, R.string.tab_text_history};

    private static final int TAB_POSITION_DASHBOARD = 0;
    private static final int TAB_POSITION_HISTORY = 1;

    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;

        if (position == TAB_POSITION_DASHBOARD)
        {
            fragment = new DashboardFragment();
        } else if (position == TAB_POSITION_HISTORY) {
            fragment = new HistoryFragment();
        }

        return fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }
}