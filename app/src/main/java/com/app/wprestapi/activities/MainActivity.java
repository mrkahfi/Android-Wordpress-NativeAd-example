package com.app.wprestapi.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.wprestapi.R;
import com.app.wprestapi.fragments.FragmentCategory;
import com.app.wprestapi.fragments.FragmentPost;
import com.app.wprestapi.fragments.FragmentSampleNative;
import com.app.wprestapi.utils.AppBarLayoutBehavior;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView navigation;
    private ViewPager viewPager;
    private Toolbar toolbar;
    MenuItem prevMenuItem;
    int pager_number = 3;
    private long exitTime = 0;
    CoordinatorLayout parent_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initAds();
    }

    public void showSnackBar(String msg) {
        Snackbar.make(parent_view, msg, Snackbar.LENGTH_SHORT).show();
    }

    public void initView() {
        parent_view = findViewById(R.id.tab_coordinator_layout);
        AppBarLayout appBarLayout = findViewById(R.id.tab_appbar_layout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);

        navigation = findViewById(R.id.navigation);
        navigation.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);

        viewPager = findViewById(R.id.viewpager);

        initViewPager();

    }

    public void initAds() {
        MobileAds.initialize(this, initializationStatus -> {
        });
    }

    public void initViewPager() {
        viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(pager_number);
        navigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_recent:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_category:
                    viewPager.setCurrentItem(1);
                    return true;
                case R.id.navigation_favorite:
                    viewPager.setCurrentItem(2);
                    return true;
            }
            return false;
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                navigation.getMenu().getItem(position).setChecked(true);
                prevMenuItem = navigation.getMenu().getItem(position);

                if (viewPager.getCurrentItem() == 1) {
                    toolbar.setTitle(getResources().getString(R.string.title_nav_category));
                } else if (viewPager.getCurrentItem() == 2) {
                    toolbar.setTitle(getResources().getString(R.string.title_nav_favorite));
                } else {
                    toolbar.setTitle(R.string.app_name);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new FragmentPost();
                case 1:
                    return new FragmentCategory();
                case 2:
                    return new FragmentSampleNative();
            }
            return new FragmentPost();
        }

        @Override
        public int getCount() {
            return pager_number;
        }

    }


}