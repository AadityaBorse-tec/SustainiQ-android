package com.sustainiq.app.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.sustainiq.app.R;
import com.sustainiq.app.ui.auth.AuthActivity;
import com.sustainiq.app.util.SessionManager;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private MaterialButton btnNext;
    private MaterialButton btnSkip;
    private LinearLayout dotContainer;
    private ImageView[] dots;
    private OnboardingAdapter adapter;
    private SessionManager session;

    private static final int PAGE_COUNT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        session = new SessionManager(this);
        viewPager    = findViewById(R.id.onboarding_viewpager);
        btnNext      = findViewById(R.id.btn_next);
        btnSkip      = findViewById(R.id.btn_skip);
        dotContainer = findViewById(R.id.dot_container);

        adapter = new OnboardingAdapter(this);
        viewPager.setAdapter(adapter);

        setupDots(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
                if (position == PAGE_COUNT - 1) {
                    btnNext.setText(R.string.get_started);
                    btnSkip.setVisibility(View.GONE);
                } else {
                    btnNext.setText(R.string.next);
                    btnSkip.setVisibility(View.VISIBLE);
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < PAGE_COUNT - 1) {
                viewPager.setCurrentItem(current + 1);
            } else {
                finishOnboarding();
            }
        });

        btnSkip.setOnClickListener(v -> finishOnboarding());
    }

    private void setupDots(int selectedIndex) {
        dots = new ImageView[PAGE_COUNT];
        dotContainer.removeAllViews();

        for (int i = 0; i < PAGE_COUNT; i++) {
            dots[i] = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    i == selectedIndex ? 24 : 8, 8);
            params.setMargins(4, 0, 4, 0);
            dots[i].setLayoutParams(params);
            dots[i].setBackgroundResource(
                    i == selectedIndex
                            ? R.drawable.dot_active
                            : R.drawable.dot_inactive);
            dotContainer.addView(dots[i]);
        }
    }

    private void updateDots(int selectedIndex) {
        for (int i = 0; i < PAGE_COUNT; i++) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dots[i].getLayoutParams();
            params.width = i == selectedIndex ? 24 : 8;
            dots[i].setLayoutParams(params);
            dots[i].setBackgroundResource(
                    i == selectedIndex
                            ? R.drawable.dot_active
                            : R.drawable.dot_inactive);
        }
    }

    private void finishOnboarding() {
        session.setOnboardingDone(true);
        startActivity(new Intent(this, AuthActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
