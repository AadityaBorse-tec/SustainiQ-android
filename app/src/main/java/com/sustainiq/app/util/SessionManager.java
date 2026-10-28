package com.sustainiq.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.sustainiq.app.data.model.UserRole;

public class SessionManager {

    private static final String PREFS_NAME     = "sustainiq_session";
    private static final String KEY_LOGGED_IN  = "is_logged_in";
    private static final String KEY_ONBOARDING = "onboarding_done";
    private static final String KEY_NAME       = "user_name";
    private static final String KEY_EMAIL      = "user_email";
    private static final String KEY_ROLE       = "user_role";
    private static final String KEY_USER_ID    = "user_id";
    private static final String KEY_LANGUAGE   = "language";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs  = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // ─── Onboarding ──────────────────────────────────────────────────────────

    public boolean isOnboardingDone() { return prefs.getBoolean(KEY_ONBOARDING, false); }
    public void    setOnboardingDone(boolean done) { editor.putBoolean(KEY_ONBOARDING, done).apply(); }
    public void    setOnboardingDone() { setOnboardingDone(true); }

    // ─── Login ───────────────────────────────────────────────────────────────

    public void saveLogin(String name, String email, UserRole role) {
        editor.putBoolean(KEY_LOGGED_IN, true)
              .putString(KEY_NAME,  name)
              .putString(KEY_EMAIL, email)
              .putString(KEY_ROLE,  role.name())
              .apply();
    }

    public void setUserId(int id) { editor.putInt(KEY_USER_ID, id).apply(); }

    public boolean  isLoggedIn()   { return prefs.getBoolean(KEY_LOGGED_IN, false); }
    public String   getUserName()  { return prefs.getString(KEY_NAME,  "User"); }
    public String   getUserEmail() { return prefs.getString(KEY_EMAIL, ""); }
    public int      getUserId()    { return prefs.getInt(KEY_USER_ID, -1); }

    public UserRole getUserRole() {
        String r = prefs.getString(KEY_ROLE, "CITIZEN");
        try { return UserRole.valueOf(r); }
        catch (Exception e) { return UserRole.CITIZEN; }
    }

    // ─── Language ────────────────────────────────────────────────────────────

    public String getLanguage() { return prefs.getString(KEY_LANGUAGE, "en"); }
    public void   setLanguage(String lang) { editor.putString(KEY_LANGUAGE, lang).apply(); }

    // ─── Logout ──────────────────────────────────────────────────────────────

    public void logout() {
        editor.putBoolean(KEY_LOGGED_IN, false)
              .remove(KEY_NAME)
              .remove(KEY_EMAIL)
              .remove(KEY_ROLE)
              .remove(KEY_USER_ID)
              .apply();
    }
}
