package com.sustainiq.app.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sustainiq.app.R;
import com.sustainiq.app.ui.community.CommunityFragment;
import com.sustainiq.app.ui.home.HomeFragment;
import com.sustainiq.app.ui.impact.ImpactFragment;
import com.sustainiq.app.ui.profile.ProfileFragment;
import com.sustainiq.app.ui.reports.CreateReportFragment;
import com.sustainiq.app.ui.reports.MyReportsFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.sustainiq.app.util.SessionManager session = new com.sustainiq.app.util.SessionManager(this);
        if (!session.isLoggedIn()) {
            Intent intent = new Intent(this, com.sustainiq.app.ui.auth.AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        } else if (session.getUserRole() == com.sustainiq.app.data.model.UserRole.ADMINISTRATOR) {
            Intent intent = new Intent(this, com.sustainiq.app.ui.admin.AdminDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main_dashboard);

        bottomNav = findViewById(R.id.bottom_nav);

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
            } else if (id == R.id.nav_profile) {
                selected = new ProfileFragment();
            } else {
                return false;
            }

            loadFragment(selected);
            return true;
        });

        // Load initial screen
        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().hasExtra("nav_tab")) {
                handleIntent(getIntent());
            } else {
                loadFragment(new HomeFragment());
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    public void handleIntent(Intent intent) {
        if (intent == null) return;
        String navTab = intent.getStringExtra("nav_tab");
        if (navTab == null) return;

        switch (navTab) {
            case "report":
                String category = intent.getStringExtra("category");
                String title = intent.getStringExtra("title");
                CreateReportFragment reportFrag = CreateReportFragment.newInstance(category, title);
                navigateToFragment(reportFrag, true);
                break;
            case "my_reports":
                setSelectedTab(R.id.nav_my_reports);
                break;
            case "community":
                setSelectedTab(R.id.nav_community);
                break;
            case "impact":
                setSelectedTab(R.id.nav_impact);
                break;
            case "profile":
                setSelectedTab(R.id.nav_profile);
                break;
            case "home":
                setSelectedTab(R.id.nav_home);
                break;
        }
    }

    public void setSelectedTab(int menuItemId) {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(menuItemId);
        }
    }

    public void navigateToFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        tx.replace(R.id.fragment_container, fragment);
        if (addToBackStack) {
            tx.addToBackStack(null);
        }
        tx.commit();
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        tx.replace(R.id.fragment_container, fragment);
        tx.commit();
    }
}
