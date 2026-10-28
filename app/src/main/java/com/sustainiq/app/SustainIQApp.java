package com.sustainiq.app;

import android.app.Application;
import android.content.SharedPreferences;

import com.sustainiq.app.data.db.AppDatabase;

public class SustainIQApp extends Application {

    private static SustainIQApp instance;
    private static SharedPreferences prefs;
    private AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        prefs = getSharedPreferences("sustainiq_prefs", MODE_PRIVATE);
        // Initialise Room DB and seed initial data if empty
        database = AppDatabase.getInstance(this);
        com.sustainiq.app.data.db.DataSeeder.seedIfEmpty(this);
    }

    public static SustainIQApp getInstance() { return instance; }
    public static SharedPreferences getPrefs() { return prefs; }
    public AppDatabase getDatabase() { return database; }
}
