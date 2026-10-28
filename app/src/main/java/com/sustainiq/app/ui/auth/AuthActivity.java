package com.sustainiq.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.sustainiq.app.R;
import com.sustainiq.app.data.model.UserRole;
import com.sustainiq.app.ui.main.MainActivity;
import com.sustainiq.app.util.SessionManager;

public class AuthActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etName;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        session    = new SessionManager(this);
        etEmail    = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etName     = findViewById(R.id.et_name);

        MaterialButton btnSignIn = findViewById(R.id.btn_sign_in);
        MaterialButton btnDemo   = findViewById(R.id.btn_demo);

        btnSignIn.setOnClickListener(v -> attemptLogin());
        btnDemo.setOnClickListener(v -> loginAsDemo());
    }

    private void attemptLogin() {
        String email    = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String name     = etName.getText() != null ? etName.getText().toString().trim() : "Aaditya";

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 4) {
            Toast.makeText(this, "Password too short", Toast.LENGTH_SHORT).show();
            return;
        }

        String displayName = TextUtils.isEmpty(name) ? getFirstPart(email) : name;
        session.saveLogin(displayName, email, UserRole.CITIZEN);
        launchMain();
    }

    private void loginAsDemo() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        if (TextUtils.isEmpty(name)) name = "Aaditya";
        session.saveLogin(name, "demo@sustainiq.app", UserRole.CITIZEN);
        launchMain();
    }

    private void launchMain() {
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private String getFirstPart(String email) {
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? capitalize(email.substring(0, atIndex)) : "User";
    }

    private String capitalize(String s) {
        if (TextUtils.isEmpty(s)) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
