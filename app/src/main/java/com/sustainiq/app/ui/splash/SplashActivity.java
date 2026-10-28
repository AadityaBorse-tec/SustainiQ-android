package com.sustainiq.app.ui.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.sustainiq.app.R;
import com.sustainiq.app.ui.onboarding.OnboardingActivity;
import com.sustainiq.app.ui.main.MainActivity;
import com.sustainiq.app.util.SessionManager;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final long LOGO_FADE_DURATION = 700;
    private static final long TAGLINE_DELAY = 500;
    private static final long TAGLINE_FADE_DURATION = 500;
    private static final long NAVIGATE_DELAY = 1800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View logoContainer = findViewById(R.id.logo_container);
        View taglineText   = findViewById(R.id.tagline_text);

        // Fade in logo
        logoContainer.animate()
                .alpha(1f)
                .setDuration(LOGO_FADE_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Fade in tagline slightly after
        new Handler(Looper.getMainLooper()).postDelayed(() ->
            taglineText.animate()
                    .alpha(1f)
                    .setDuration(TAGLINE_FADE_DURATION)
                    .setInterpolator(new DecelerateInterpolator())
                    .start(),
            TAGLINE_DELAY
        );

        // Navigate after total display time
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateNext, NAVIGATE_DELAY);
    }

    private void navigateNext() {
        SessionManager session = new SessionManager(this);
        Intent intent;

        if (!session.isOnboardingDone()) {
            intent = new Intent(this, OnboardingActivity.class);
        } else if (session.isLoggedIn()) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, OnboardingActivity.class);
        }

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
