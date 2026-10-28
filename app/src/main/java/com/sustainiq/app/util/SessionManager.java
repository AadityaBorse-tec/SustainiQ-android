package com.sustainiq.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.sustainiq.app.data.model.UserRole;

public class SessionManager {

    private static final String PREFS_NAME = "sustainiq_prefs";
    private static final String KEY_ONBOARDING_DONE = "onboarding_done";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // ---- Onboarding ----
    public boolean isOnboardingDone() {
        return prefs.getBoolean(KEY_ONBOARDING_DONE, false);
    }

    public void setOnboardingDone(boolean done) {
        editor.putBoolean(KEY_ONBOARDING_DONE, done).apply();
    }

    // ---- Login ----
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public void saveLogin(String name, String email, UserRole role) {
        editor.putBoolean(KEY_LOGGED_IN, true)
              .putString(KEY_USER_NAME, name)
              .putString(KEY_USER_EMAIL, email)
              .putString(KEY_USER_ROLE, role.name())
              .apply();
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "Aaditya");
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public UserRole getUserRole() {
        String roleStr = prefs.getString(KEY_USER_ROLE, UserRole.CITIZEN.name());
        try {
            return UserRole.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            return UserRole.CITIZEN;
        }
    }

    public void logout() {
        editor.putBoolean(KEY_LOGGED_IN, false)
              .remove(KEY_USER_NAME)
              .remove(KEY_USER_EMAIL)
              .remove(KEY_USER_ROLE)
              .apply();
    }
}
