package com.sustainiq.app.ui.reports;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.sustainiq.app.R;
import com.sustainiq.app.data.db.AppDatabase;
import com.sustainiq.app.data.db.ReportEntity;
import com.sustainiq.app.data.db.StatusHistoryEntity;
import com.sustainiq.app.data.model.UserRole;
import com.sustainiq.app.data.repository.ReportRepository;
import com.sustainiq.app.util.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportDetailActivity extends AppCompatActivity {

    public static final String EXTRA_REPORT_ID = "extra_report_id";

    private MaterialToolbar toolbar;
    private TextView tvId, tvStatusBadge, tvTitle, tvCategoryPriority, tvDesc, tvLocationFull;
    private ImageView imgPhoto;
    private MaterialButton btnOpenMaps, btnUpvote, btnAdminAction;
    private LinearLayout layoutTimeline;

    private String reportId = "";
    private ReportEntity currentReport;
    private ReportRepository repository;
    private SessionManager session;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);

        repository = new ReportRepository(this);
        session    = new SessionManager(this);

        reportId = getIntent().getStringExtra(EXTRA_REPORT_ID);
        if (reportId == null || reportId.isEmpty()) {
            Toast.makeText(this, "Report ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupToolbar();
        observeReportData();
        observeTimeline();
    }

    private void bindViews() {
        toolbar            = findViewById(R.id.toolbar_detail);
        tvId               = findViewById(R.id.tv_detail_id);
        tvStatusBadge      = findViewById(R.id.tv_detail_status_badge);
        tvTitle            = findViewById(R.id.tv_detail_title);
        tvCategoryPriority = findViewById(R.id.tv_detail_category_priority);
        tvDesc             = findViewById(R.id.tv_detail_desc);
        tvLocationFull     = findViewById(R.id.tv_detail_location_full);
        imgPhoto           = findViewById(R.id.img_detail_photo);
        btnOpenMaps        = findViewById(R.id.btn_open_maps);
        btnUpvote          = findViewById(R.id.btn_upvote);
        btnAdminAction     = findViewById(R.id.btn_action_admin);
        layoutTimeline     = findViewById(R.id.layout_timeline_container);

        btnOpenMaps.setOnClickListener(v -> openMaps());
        btnUpvote.setOnClickListener(v -> handleUpvote());
        btnAdminAction.setOnClickListener(v -> handleAdminAction());
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void observeReportData() {
        repository.getReportDao().getReportById(reportId).observe(this, report -> {
            if (report == null) return;
            currentReport = report;

            tvId.setText(report.reportId);
            tvTitle.setText(report.title);
            tvDesc.setText(report.description);

            // Category & Priority
            String cat = report.category != null ? report.category : "OTHER";
            String pri = report.priority != null ? report.priority : "MEDIUM";
            tvCategoryPriority.setText(cat + "  •  " + pri + " Priority");

            // Status Badge
            String status = report.status != null ? report.status : "REPORTED";
            tvStatusBadge.setText(status.replace('_', ' '));
            applyStatusColor(status);

            // Location
            StringBuilder loc = new StringBuilder();
            if (report.locality != null && !report.locality.isEmpty()) loc.append(report.locality).append(", ");
            if (report.city != null && !report.city.isEmpty()) loc.append(report.city).append(", ");
            if (report.district != null && !report.district.isEmpty()) loc.append(report.district).append(", ");
            if (report.state != null && !report.state.isEmpty()) loc.append(report.state);
            if (report.pinCode != null && !report.pinCode.isEmpty()) loc.append(" - ").append(report.pinCode);
            tvLocationFull.setText(loc.toString());

            // Photo
            if (report.photoUris != null && !report.photoUris.isEmpty()) {
                try {
                    imgPhoto.setImageURI(Uri.parse(report.photoUris));
                    imgPhoto.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    imgPhoto.setVisibility(View.GONE);
                }
            } else {
                imgPhoto.setVisibility(View.GONE);
            }

            // Upvotes
            btnUpvote.setText("Endorse / Upvote (" + report.upvotes + ")");

            // Role action button label — only visible to Administrators and Field Workers
            UserRole role = session.getUserRole();
            if (role == UserRole.ADMINISTRATOR) {
                btnAdminAction.setVisibility(View.VISIBLE);
                btnAdminAction.setText("Update Status / Triage");
            } else if (role == UserRole.FIELD_WORKER) {
                btnAdminAction.setVisibility(View.VISIBLE);
                btnAdminAction.setText("Update Progress");
            } else {
                btnAdminAction.setVisibility(View.GONE);
            }
        });
    }

    private void applyStatusColor(String status) {
        switch (status) {
            case "REPORTED":
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_reported);
                tvStatusBadge.setTextColor(Color.parseColor("#4A5E4D"));
                break;
            case "TRIAGED":
            case "ASSIGNED":
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_assigned);
                tvStatusBadge.setTextColor(Color.parseColor("#5E35B1"));
                break;
            case "IN_PROGRESS":
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_in_progress);
                tvStatusBadge.setTextColor(Color.parseColor("#E65100"));
                break;
            case "VERIFIED":
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_verified);
                tvStatusBadge.setTextColor(Color.parseColor("#2E7D32"));
                break;
            case "REJECTED":
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_rejected);
                tvStatusBadge.setTextColor(Color.parseColor("#C62828"));
                break;
            default:
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_reported);
                tvStatusBadge.setTextColor(Color.parseColor("#4A5E4D"));
                break;
        }
    }

    private void observeTimeline() {
        repository.getHistoryDao().getHistoryForReport(reportId).observe(this, this::renderTimeline);
    }

    private void renderTimeline(List<StatusHistoryEntity> historyList) {
        layoutTimeline.removeAllViews();
        if (historyList == null || historyList.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("No status transitions recorded yet.");
            tv.setTextColor(Color.parseColor("#7A9A7E"));
            layoutTimeline.addView(tv);
            return;
        }

        for (int i = 0; i < historyList.size(); i++) {
            StatusHistoryEntity item = historyList.get(i);
            View stepView = LayoutInflater.from(this).inflate(R.layout.item_timeline_step, layoutTimeline, false);

            TextView tvStepStatus = stepView.findViewById(R.id.tv_step_status);
            TextView tvStepActor  = stepView.findViewById(R.id.tv_step_actor);
            TextView tvStepNote   = stepView.findViewById(R.id.tv_step_note);
            TextView tvStepDate   = stepView.findViewById(R.id.tv_step_date);
            View viewLine         = stepView.findViewById(R.id.view_step_line);

            tvStepStatus.setText(item.newStatus.replace('_', ' '));
            tvStepActor.setText("By " + item.actorName + " (" + item.actorRole + ")");
            tvStepNote.setText(item.note != null && !item.note.isEmpty() ? item.note : "Status updated");
            tvStepDate.setText(dateFormat.format(new Date(item.timestamp)));

            // Hide connecting line on last item
            if (i == historyList.size() - 1) {
                viewLine.setVisibility(View.INVISIBLE);
            }

            layoutTimeline.addView(stepView);
        }
    }

    private void openMaps() {
        if (currentReport == null) return;
        double lat = currentReport.latitude != 0 ? currentReport.latitude : 18.5204;
        double lng = currentReport.longitude != 0 ? currentReport.longitude : 73.8567;
        Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng + "(" + Uri.encode(currentReport.title) + ")");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        try {
            startActivity(mapIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Coordinates: " + lat + ", " + lng, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleUpvote() {
        if (currentReport == null) return;
        repository.getExecutor().execute(() -> {
            repository.getReportDao().incrementUpvote(currentReport.reportId);
            runOnUiThread(() -> Toast.makeText(this, "Thank you for endorsing this report.", Toast.LENGTH_SHORT).show());
        });
    }

    private void handleAdminAction() {
        if (currentReport == null) return;
        final String[] statuses = {"TRIAGED", "ASSIGNED", "IN_PROGRESS", "VERIFIED", "REJECTED"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Municipal Triage: " + currentReport.reportId);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 12);

        TextView tvPrompt = new TextView(this);
        tvPrompt.setText("Select target workflow status and add official dispatch/verification note:");
        tvPrompt.setTextSize(13);
        tvPrompt.setTextColor(Color.parseColor("#4A5E4D"));
        tvPrompt.setPadding(0, 0, 0, 16);
        layout.addView(tvPrompt);

        final EditText etNote = new EditText(this);
        etNote.setHint("Action / Dispatch / Verification note");
        layout.addView(etNote);
        builder.setView(layout);

        builder.setSingleChoiceItems(statuses, 0, (dialog, which) -> {
            dialog.dismiss();
            String chosen = statuses[which];
            String note = etNote.getText().toString().trim();
            if (note.isEmpty()) {
                if ("ASSIGNED".equals(chosen)) {
                    note = "Assigned to Ward Sanitation & Maintenance Unit";
                } else if ("TRIAGED".equals(chosen)) {
                    note = "Verified location, jurisdiction and severity";
                } else if ("VERIFIED".equals(chosen)) {
                    note = "Resolution confirmed clean by municipal authority";
                } else {
                    note = "Status updated to " + chosen;
                }
            }

            String actorName = session.getUserName();
            String actorRole = session.getUserRole().name();

            repository.updateReportStatus(currentReport.reportId, chosen, actorName, actorRole, note);
            Toast.makeText(this, "Report updated to " + chosen, Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
