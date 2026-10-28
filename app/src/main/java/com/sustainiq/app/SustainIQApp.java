package com.sustainiq.app;

import android.app.Application;
import android.content.SharedPreferences;

public class SustainIQApp extends Application {

    private static SustainIQApp instance;
    private static final String PREFS_NAME = "sustainiq_prefs";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static SustainIQApp getInstance() {
        return instance;
    }

    public SharedPreferences getPrefs() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }
}
