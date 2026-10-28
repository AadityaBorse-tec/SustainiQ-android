package com.sustainiq.app.ui.splash;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.sustainiq.app.R;
import com.sustainiq.app.ui.auth.AuthActivity;
import com.sustainiq.app.ui.main.MainActivity;
import com.sustainiq.app.ui.onboarding.OnboardingActivity;
import com.sustainiq.app.util.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION_MS = 2400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full-screen, no status bar
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        View imgSplash  = findViewById(R.id.img_splash);
        View tvVersion  = findViewById(R.id.tv_version);

        // Fade-in the official splash image
        ObjectAnimator imageFade = ObjectAnimator.ofFloat(imgSplash, "alpha", 0f, 1f);
        imageFade.setDuration(900);
        imageFade.setInterpolator(new AccelerateDecelerateInterpolator());

        // Slight scale-up for a premium reveal feel
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(imgSplash, "scaleX", 1.04f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(imgSplash, "scaleY", 1.04f, 1f);
        scaleX.setDuration(900);
        scaleY.setDuration(900);

        // Version label fades in after image
        ObjectAnimator versionFade = ObjectAnimator.ofFloat(tvVersion, "alpha", 0f, 0.7f);
        versionFade.setDuration(600);
        versionFade.setStartDelay(700);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(imageFade, scaleX, scaleY, versionFade);
        set.start();

        new Handler(Looper.getMainLooper()).postDelayed(this::navigateNext, SPLASH_DURATION_MS);
    }

    private void navigateNext() {
        SessionManager session = new SessionManager(this);
        Intent intent;

        if (!session.isOnboardingDone()) {
            intent = new Intent(this, OnboardingActivity.class);
        } else if (!session.isLoggedIn()) {
            intent = new Intent(this, AuthActivity.class);
        } else if (session.getUserRole() == com.sustainiq.app.data.model.UserRole.ADMINISTRATOR) {
            intent = new Intent(this, com.sustainiq.app.ui.admin.AdminDashboardActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
