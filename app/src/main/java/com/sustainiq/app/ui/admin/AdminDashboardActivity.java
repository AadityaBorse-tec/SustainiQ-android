package com.sustainiq.app.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.sustainiq.app.R;
import com.sustainiq.app.data.db.AppDatabase;
import com.sustainiq.app.data.db.ReportEntity;
import com.sustainiq.app.data.model.UserRole;
import com.sustainiq.app.ui.auth.AuthActivity;
import com.sustainiq.app.ui.reports.ReportAdapter;
import com.sustainiq.app.ui.reports.ReportDetailActivity;
import com.sustainiq.app.util.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Functional Municipal Administrative Dashboard.
 * Displays genuine statistics calculated from the SQLite Room database,
 * enables report triage, workflow status updates, and field dispatch.
 */
public class AdminDashboardActivity extends AppCompatActivity implements ReportAdapter.OnReportClickListener {

    private SessionManager session;
    private AppDatabase db;
    private ReportAdapter adapter;

    private TextView tvStatTotal, tvStatPending, tvStatResolved;
    private RecyclerView rvReports;
    private LinearLayout layoutEmpty;

    private Chip chipAll, chipReported, chipProgress, chipResolved;
    private String currentFilter = "ALL";
    private List<ReportEntity> allAdminReports = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);

        // Security check: only authorized administrators may enter
        if (!session.isLoggedIn() || session.getUserRole() != UserRole.ADMINISTRATOR) {
            Toast.makeText(this, "Unauthorized access. Administrator privileges required.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_dashboard);
        db = AppDatabase.getInstance(this);

        initViews();
        setupRecyclerView();
        setupFilters();
        observeReports();
        loadDatabaseStatistics();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDatabaseStatistics();
    }

    private void initViews() {
        tvStatTotal    = findViewById(R.id.tv_admin_stat_total);
        tvStatPending  = findViewById(R.id.tv_admin_stat_pending);
        tvStatResolved = findViewById(R.id.tv_admin_stat_resolved);
        rvReports      = findViewById(R.id.rv_admin_reports);
        layoutEmpty    = findViewById(R.id.layout_admin_empty);

        chipAll        = findViewById(R.id.chip_admin_all);
        chipReported   = findViewById(R.id.chip_admin_reported);
        chipProgress   = findViewById(R.id.chip_admin_progress);
        chipResolved   = findViewById(R.id.chip_admin_resolved);

        MaterialButton btnLogout = findViewById(R.id.btn_admin_logout);
        btnLogout.setOnClickListener(v -> performSignOut());
    }

    private void setupRecyclerView() {
        adapter = new ReportAdapter(this);
        rvReports.setLayoutManager(new LinearLayoutManager(this));
        rvReports.setAdapter(adapter);
    }

    private void setupFilters() {
        chipAll.setOnClickListener(v -> {
            currentFilter = "ALL";
            applyFilter();
        });
        chipReported.setOnClickListener(v -> {
            currentFilter = "REPORTED";
            applyFilter();
        });
        chipProgress.setOnClickListener(v -> {
            currentFilter = "IN_PROGRESS";
            applyFilter();
        });
        chipResolved.setOnClickListener(v -> {
            currentFilter = "VERIFIED";
            applyFilter();
        });
    }

    private void observeReports() {
        db.reportDao().getAllReports().observe(this, reports -> {
            allAdminReports = reports != null ? reports : new ArrayList<>();
            applyFilter();
            loadDatabaseStatistics();
        });
    }

    private void applyFilter() {
        List<ReportEntity> filtered = new ArrayList<>();
        for (ReportEntity r : allAdminReports) {
            if ("ALL".equals(currentFilter)) {
                filtered.add(r);
            } else if ("REPORTED".equals(currentFilter)) {
                if ("REPORTED".equalsIgnoreCase(r.status) || "TRIAGED".equalsIgnoreCase(r.status)) {
                    filtered.add(r);
                }
            } else if ("IN_PROGRESS".equals(currentFilter)) {
                if ("IN_PROGRESS".equalsIgnoreCase(r.status) || "ASSIGNED".equalsIgnoreCase(r.status)) {
                    filtered.add(r);
                }
            } else if ("VERIFIED".equals(currentFilter)) {
                if ("VERIFIED".equalsIgnoreCase(r.status) || "SUBMITTED".equalsIgnoreCase(r.status)) {
                    filtered.add(r);
                }
            }
        }

        adapter.setReports(filtered);
        if (filtered.isEmpty()) {
            rvReports.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvReports.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void loadDatabaseStatistics() {
        Executors.newSingleThreadExecutor().execute(() -> {
            int total = db.reportDao().totalReports();
            int pending = db.reportDao().totalPending();
            int verified = db.reportDao().totalVerified();

            runOnUiThread(() -> {
                tvStatTotal.setText(String.valueOf(total));
                tvStatPending.setText(String.valueOf(pending));
                tvStatResolved.setText(String.valueOf(verified));
            });
        });
    }

    @Override
    public void onReportClick(ReportEntity report) {
        Intent intent = new Intent(this, ReportDetailActivity.class);
        intent.putExtra("report_id", report.reportId);
        startActivity(intent);
    }

    private void performSignOut() {
        session.logout();
        Toast.makeText(this, "Signed out from Municipal Admin session", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
