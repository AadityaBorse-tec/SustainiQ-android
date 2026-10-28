package com.sustainiq.app.ui.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sustainiq.app.R;
import com.sustainiq.app.ui.community.CommunityFragment;
import com.sustainiq.app.ui.home.HomeFragment;
import com.sustainiq.app.ui.impact.ImpactFragment;
import com.sustainiq.app.ui.reports.MyReportsFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dashboard);

        bottomNav = findViewById(R.id.bottom_nav);

        // Load home fragment on first launch
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selected = new HomeFragment();
            } else if (id == R.id.nav_my_reports) {
                selected = new MyReportsFragment();
            } else if (id == R.id.nav_community) {
                selected = new CommunityFragment();
            } else if (id == R.id.nav_impact) {
                selected = new ImpactFragment();
            } else {
                return false;
            }

            loadFragment(selected);
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        tx.replace(R.id.fragment_container, fragment);
        tx.commit();
    }
}
