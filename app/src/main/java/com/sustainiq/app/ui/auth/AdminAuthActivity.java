package com.sustainiq.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sustainiq.app.R;
import com.sustainiq.app.data.model.UserRole;
import com.sustainiq.app.ui.admin.AdminDashboardActivity;
import com.sustainiq.app.util.SessionManager;

/**
 * Dedicated administrative authentication screen.
 * For local development prototype, validates the required security code 'aaditya7112'.
 */
public class AdminAuthActivity extends AppCompatActivity {

    // Development prototype required code
    private static final String REQUIRED_PROTOTYPE_CODE = "aaditya7112";

    private TextInputLayout tilAdminCode;
    private TextInputEditText etAdminCode;
    private MaterialButton btnContinue, btnBack;
    private ImageView btnBackIcon;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_auth);

        session = new SessionManager(this);

        tilAdminCode = findViewById(R.id.til_admin_code);
        etAdminCode  = findViewById(R.id.et_admin_code);
        btnContinue  = findViewById(R.id.btn_admin_continue);
        btnBack      = findViewById(R.id.btn_admin_back);
        btnBackIcon  = findViewById(R.id.btn_back_icon);

        btnContinue.setOnClickListener(v -> validateAndProceed());
        btnBack.setOnClickListener(v -> finishAndReturn());
        btnBackIcon.setOnClickListener(v -> finishAndReturn());

        etAdminCode.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validateAndProceed();
                return true;
            }
            return false;
        });
    }

    private void validateAndProceed() {
        String entered = etAdminCode.getText() != null ? etAdminCode.getText().toString().trim() : "";

        if (TextUtils.isEmpty(entered)) {
            tilAdminCode.setError("Please enter the Admin Access Code");
            return;
        }

        // Validate entered code strictly against the required prototype authorization code
        if (REQUIRED_PROTOTYPE_CODE.equals(entered)) {
            tilAdminCode.setError(null);

            // Grant administrator credentials in active session
            session.saveLogin(
                    "Municipal Administrator",
                    "admin.pmc@sustainiq.gov.in",
                    UserRole.ADMINISTRATOR
            );
            session.setUserId(900);

            Toast.makeText(this, "Admin verified: Municipal Control Center opened.", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, AdminDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } else {
            tilAdminCode.setError("Invalid Admin Access Code. Access Denied.");
            Toast.makeText(this, "Access Denied: Invalid administrator security code.", Toast.LENGTH_SHORT).show();
        }
    }

    private void finishAndReturn() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
