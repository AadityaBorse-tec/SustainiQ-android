package com.sustainiq.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sustainiq.app.R;
import com.sustainiq.app.data.db.AppDatabase;
import com.sustainiq.app.data.db.UserEntity;
import com.sustainiq.app.ui.main.MainActivity;
import com.sustainiq.app.ui.onboarding.OnboardingActivity;
import com.sustainiq.app.util.AppUtils;
import com.sustainiq.app.util.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthActivity extends AppCompatActivity {

    // Sign-in views
    private TextInputEditText etLoginEmail, etLoginPassword;
    private TextInputLayout   tilLoginEmail, tilLoginPassword;

    // Register views
    private TextInputEditText etRegName, etRegEmail, etRegPhone, etRegPassword, etRegConfirm;
    private TextInputLayout   tilRegName, tilRegEmail, tilRegPhone, tilRegPassword, tilRegConfirm;

    private LinearLayout layoutLogin, layoutRegister;
    private SessionManager session;
    private AppDatabase    db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        session = new SessionManager(this);
        db      = AppDatabase.getInstance(this);

        // Tab toggle
        TabLayout tabAuth = findViewById(R.id.tab_auth);
        tabAuth.addTab(tabAuth.newTab().setText("Sign In"));
        tabAuth.addTab(tabAuth.newTab().setText("Register"));

        layoutLogin    = findViewById(R.id.layout_login);
        layoutRegister = findViewById(R.id.layout_register);

        tabAuth.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    layoutLogin.setVisibility(View.VISIBLE);
                    layoutRegister.setVisibility(View.GONE);
                } else {
                    layoutLogin.setVisibility(View.GONE);
                    layoutRegister.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Sign-in fields
        etLoginEmail    = findViewById(R.id.et_login_email);
        etLoginPassword = findViewById(R.id.et_login_password);
        tilLoginEmail   = findViewById(R.id.til_login_email);
        tilLoginPassword= findViewById(R.id.til_login_password);

        // Register fields
        etRegName    = findViewById(R.id.et_reg_name);
        etRegEmail   = findViewById(R.id.et_reg_email);
        etRegPhone   = findViewById(R.id.et_reg_phone);
        etRegPassword= findViewById(R.id.et_reg_password);
        etRegConfirm = findViewById(R.id.et_reg_confirm);
        tilRegName   = findViewById(R.id.til_reg_name);
        tilRegEmail  = findViewById(R.id.til_reg_email);
        tilRegPhone  = findViewById(R.id.til_reg_phone);
        tilRegPassword = findViewById(R.id.til_reg_password);
        tilRegConfirm  = findViewById(R.id.til_reg_confirm);

        MaterialButton btnSignIn    = findViewById(R.id.btn_sign_in);
        MaterialButton btnRegister  = findViewById(R.id.btn_register);
        MaterialButton btnAdminLogin = findViewById(R.id.btn_admin_login);

        btnSignIn.setOnClickListener(v -> attemptLogin());
        btnRegister.setOnClickListener(v -> attemptRegister());
        if (btnAdminLogin != null) {
            btnAdminLogin.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminAuthActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
    }

    // ─── Login ───────────────────────────────────────────────────────────────

    private void attemptLogin() {
        String email = text(etLoginEmail);
        String pass  = text(etLoginPassword);
        boolean valid = true;

        if (!email.contains("@")) {
            tilLoginEmail.setError("Enter a valid email");
            valid = false;
        } else { tilLoginEmail.setError(null); }

        if (pass.length() < 4) {
            tilLoginPassword.setError("Password too short");
            valid = false;
        } else { tilLoginPassword.setError(null); }

        if (!valid) return;

        executor.execute(() -> {
            UserEntity user = db.userDao().findByEmail(email);
            runOnUiThread(() -> {
                if (user == null) {
                    tilLoginEmail.setError("No account found with this email — please register");
                    return;
                }
                String hashed = AppUtils.hashPassword(pass);
                if (!hashed.equals(user.passwordHash)) {
                    tilLoginPassword.setError("Incorrect password");
                    return;
                }
                session.saveLogin(user.name, email, parseRole(user.role));
                session.setUserId(user.id);
                Toast.makeText(this, "Welcome back, " + user.name + "!", Toast.LENGTH_SHORT).show();
                launchMain();
            });
        });
    }

    // ─── Register ────────────────────────────────────────────────────────────

    private void attemptRegister() {
        String name    = text(etRegName);
        String email   = text(etRegEmail);
        String phone   = text(etRegPhone);
        String pass    = text(etRegPassword);
        String confirm = text(etRegConfirm);
        boolean valid  = true;

        if (name.isEmpty())        { tilRegName.setError("Name required"); valid = false; }
        else tilRegName.setError(null);

        if (!email.contains("@")) { tilRegEmail.setError("Invalid email"); valid = false; }
        else tilRegEmail.setError(null);

        if (phone.length() != 10) { tilRegPhone.setError("Enter 10-digit mobile"); valid = false; }
        else tilRegPhone.setError(null);

        if (pass.length() < 6)    { tilRegPassword.setError("Min 6 characters"); valid = false; }
        else tilRegPassword.setError(null);

        if (!pass.equals(confirm)) { tilRegConfirm.setError("Passwords don't match"); valid = false; }
        else tilRegConfirm.setError(null);

        if (!valid) return;

        executor.execute(() -> {
            int exists = db.userDao().emailExists(email);
            runOnUiThread(() -> {
                if (exists > 0) {
                    tilRegEmail.setError("Email already registered");
                    return;
                }
                UserEntity user     = new UserEntity();
                user.name           = name;
                user.email          = email;
                user.phone          = phone;
                user.passwordHash   = AppUtils.hashPassword(pass);
                user.role           = "CITIZEN";
                user.language       = "en";
                user.createdAt      = System.currentTimeMillis();
                user.lastLoginAt    = user.createdAt;

                executor.execute(() -> {
                    long newId = db.userDao().insertUser(user);
                    runOnUiThread(() -> {
                        session.saveLogin(name, email,
                                com.sustainiq.app.data.model.UserRole.CITIZEN);
                        session.setUserId((int) newId);
                        Toast.makeText(this, "Welcome, " + name + "!", Toast.LENGTH_SHORT).show();
                        launchMain();
                    });
                });
            });
        });
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void launchMain() {
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private com.sustainiq.app.data.model.UserRole parseRole(String role) {
        if (role == null) return com.sustainiq.app.data.model.UserRole.CITIZEN;
        switch (role) {
            case "ADMIN":
            case "ADMINISTRATOR": return com.sustainiq.app.data.model.UserRole.ADMINISTRATOR;
            case "FIELD_WORKER":  return com.sustainiq.app.data.model.UserRole.FIELD_WORKER;
            default:              return com.sustainiq.app.data.model.UserRole.CITIZEN;
        }
    }

    @Override protected void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
