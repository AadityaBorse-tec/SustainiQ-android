package com.sustainiq.app.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.sustainiq.app.R;
import com.sustainiq.app.data.db.AppDatabase;
import com.sustainiq.app.data.db.UserEntity;
import com.sustainiq.app.data.model.UserRole;
import com.sustainiq.app.ui.assistant.AssistantActivity;
import com.sustainiq.app.ui.auth.AuthActivity;
import com.sustainiq.app.ui.main.MainActivity;
import com.sustainiq.app.util.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Personalized Profile Screen.
 * Reflects genuine authenticated citizen credentials, dynamic initials avatar,
 * real submitted reports metrics from Room DB, editable profile attributes, and secure sign-out.
 */
public class ProfileFragment extends Fragment {

    private TextView tvInitials, tvName, tvEmail, tvPhone, tvRoleBadge;
    private TextView tvStatReports, tvStatResolved, tvStatPoints;
    private View rowMyReports, rowEditProfile, rowOpenAssistant, btnOpenEditDialog;
    private MaterialButton btnLogout;

    private SessionManager session;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private UserEntity currentUserEntity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());
        db      = AppDatabase.getInstance(requireContext());

        bindViews(view);
        setupListeners();
        loadProfileData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void bindViews(View v) {
        tvInitials         = v.findViewById(R.id.tv_profile_initials);
        tvName             = v.findViewById(R.id.tv_profile_name);
        tvEmail            = v.findViewById(R.id.tv_profile_email);
        tvPhone            = v.findViewById(R.id.tv_profile_phone);
        tvRoleBadge        = v.findViewById(R.id.tv_profile_role_badge);

        tvStatReports      = v.findViewById(R.id.tv_profile_stat_reports);
        tvStatResolved     = v.findViewById(R.id.tv_profile_stat_resolved);
        tvStatPoints       = v.findViewById(R.id.tv_profile_stat_points);

        rowMyReports       = v.findViewById(R.id.row_my_reports);
        rowEditProfile     = v.findViewById(R.id.row_edit_profile);
        rowOpenAssistant   = v.findViewById(R.id.row_open_assistant);
        btnOpenEditDialog  = v.findViewById(R.id.btn_open_edit_dialog);
        btnLogout          = v.findViewById(R.id.btn_logout);
    }

    private void setupListeners() {
        if (rowMyReports != null) {
            rowMyReports.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).setSelectedTab(R.id.nav_my_reports);
                }
            });
        }

        if (rowEditProfile != null) {
            rowEditProfile.setOnClickListener(v -> showEditProfileDialog());
        }

        if (btnOpenEditDialog != null) {
            btnOpenEditDialog.setOnClickListener(v -> showEditProfileDialog());
        }

        if (rowOpenAssistant != null) {
            rowOpenAssistant.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), AssistantActivity.class));
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> performLogout());
        }
    }

    private void loadProfileData() {
        String name  = session.getUserName();
        String email = session.getUserEmail();
        UserRole role = session.getUserRole();
        int userId    = session.getUserId();

        tvName.setText(name);
        tvEmail.setText(!TextUtils.isEmpty(email) ? email : "citizen@sustainiq.app");
        tvInitials.setText(extractInitials(name));

        // Format role badge
        if (role == UserRole.ADMINISTRATOR) {
            tvRoleBadge.setText("MUNICIPAL ADMIN");
            tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_assigned);
            tvRoleBadge.setTextColor(Color.parseColor("#5E35B1"));
        } else if (role == UserRole.FIELD_WORKER) {
            tvRoleBadge.setText("FIELD WORKER");
            tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_in_progress);
            tvRoleBadge.setTextColor(Color.parseColor("#E65100"));
        } else {
            tvRoleBadge.setText("ECO CITIZEN");
            tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_verified);
            tvRoleBadge.setTextColor(Color.parseColor("#2E7D32"));
        }

        // Query database for phone number and user-specific report stats
        executor.execute(() -> {
            UserEntity user = null;
            if (userId > 0) {
                user = db.userDao().findById(userId);
            }
            if (user == null && !TextUtils.isEmpty(email)) {
                user = db.userDao().findByEmail(email);
            }
            currentUserEntity = user;

            final String phone = (user != null && !TextUtils.isEmpty(user.phone)) ? "+91 " + user.phone : "Mobile not set";
            final int countReports  = userId > 0 ? db.reportDao().countByUser(userId) : 0;
            final int countResolved = userId > 0 ? db.reportDao().countResolvedByUser(userId) : 0;
            final int points = (countResolved * 50) + (countReports * 20);

            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvPhone.setText(phone);
                    tvStatReports.setText(String.valueOf(countReports));
                    tvStatResolved.setText(String.valueOf(countResolved));
                    tvStatPoints.setText(String.valueOf(points));
                });
            }
        });
    }

    private String extractInitials(String name) {
        if (TextUtils.isEmpty(name)) return "SQ";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        } else {
            return (name.substring(0, Math.min(2, name.length()))).toUpperCase();
        }
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Personal Profile");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 16);

        TextView tvHintName = new TextView(requireContext());
        tvHintName.setText("Full Name:");
        tvHintName.setTextSize(12);
        tvHintName.setTextColor(Color.parseColor("#4A5E4D"));
        layout.addView(tvHintName);

        final EditText etName = new EditText(requireContext());
        etName.setText(session.getUserName());
        layout.addView(etName);

        TextView tvHintPhone = new TextView(requireContext());
        tvHintPhone.setText("Mobile Number (10 digits):");
        tvHintPhone.setTextSize(12);
        tvHintPhone.setTextColor(Color.parseColor("#4A5E4D"));
        tvHintPhone.setPadding(0, 20, 0, 0);
        layout.addView(tvHintPhone);

        final EditText etPhone = new EditText(requireContext());
        if (currentUserEntity != null && currentUserEntity.phone != null) {
            etPhone.setText(currentUserEntity.phone);
        }
        layout.addView(etPhone);

        builder.setView(layout);

        builder.setPositiveButton("Save Changes", (dialog, which) -> {
            String newName  = etName.getText().toString().trim();
            String newPhone = etPhone.getText().toString().trim();

            if (newName.isEmpty()) {
                Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            executor.execute(() -> {
                if (currentUserEntity != null) {
                    currentUserEntity.name = newName;
                    currentUserEntity.phone = newPhone;
                    db.userDao().updateUser(currentUserEntity);
                }
                session.saveLogin(newName, session.getUserEmail(), session.getUserRole());

                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Profile updated persistently", Toast.LENGTH_SHORT).show();
                        loadProfileData();
                    });
                }
            });
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void performLogout() {
        session.logout();
        Toast.makeText(requireContext(), "Signed out. Session cleared.", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(requireContext(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            getActivity().finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
