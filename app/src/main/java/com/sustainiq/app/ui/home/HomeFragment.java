package com.sustainiq.app.ui.home;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.sustainiq.app.R;
import com.sustainiq.app.data.db.AppDatabase;
import com.sustainiq.app.data.db.NotificationEntity;
import com.sustainiq.app.data.db.ReportEntity;
import com.sustainiq.app.ui.assistant.AssistantActivity;
import com.sustainiq.app.ui.community.CommunityFragment;
import com.sustainiq.app.ui.impact.ImpactFragment;
import com.sustainiq.app.ui.main.MainActivity;
import com.sustainiq.app.ui.notifications.NotificationsActivity;
import com.sustainiq.app.ui.profile.ProfileFragment;
import com.sustainiq.app.ui.reports.CreateReportFragment;
import com.sustainiq.app.ui.reports.MyReportsFragment;
import com.sustainiq.app.ui.reports.ReportDetailActivity;
import com.sustainiq.app.util.SessionManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private TextView tvStatTotal, tvStatResolved, tvStatImpact;
    private TextView tvRecentTitle, tvRecentStatus, tvRecentLocation;
    private TextView tvVoicePromptHint;
    private View cardRecentReport;
    private String latestReportId = null;

    private final ActivityResultLauncher<String> recordAudioPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    launchSpeechRecognizer();
                } else {
                    Toast.makeText(requireContext(), "Microphone permission is required for voice commands.", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> voiceControlLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        String spoken = matches.get(0);
                        if (tvVoicePromptHint != null) {
                            tvVoicePromptHint.setText("Heard: \"" + spoken + "\"");
                        }
                        processVoiceCommand(spoken);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager session = new SessionManager(requireContext());

        // Dynamic greeting
        TextView tvGreeting = view.findViewById(R.id.tv_greeting);
        TextView tvName     = view.findViewById(R.id.tv_user_name);

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12) {
            greeting = getString(R.string.good_morning);
        } else if (hour < 17) {
            greeting = getString(R.string.good_afternoon);
        } else {
            greeting = getString(R.string.good_evening);
        }

        tvGreeting.setText(greeting);
        tvName.setText(session.getUserName());

        // Stats views
        tvStatTotal    = view.findViewById(R.id.tv_stat_total);
        tvStatResolved = view.findViewById(R.id.tv_stat_resolved);
        tvStatImpact   = view.findViewById(R.id.tv_stat_impact);

        // Recent report views
        tvRecentTitle    = view.findViewById(R.id.tv_recent_title);
        tvRecentStatus   = view.findViewById(R.id.tv_recent_status);
        tvRecentLocation = view.findViewById(R.id.tv_recent_location);
        cardRecentReport = view.findViewById(R.id.card_recent_report);

        // Voice Control views
        tvVoicePromptHint = view.findViewById(R.id.tv_voice_prompt_hint);
        View btnVoiceControl = view.findViewById(R.id.btn_voice_control);
        View cardVoiceCommand = view.findViewById(R.id.card_voice_command);

        if (btnVoiceControl != null) {
            btnVoiceControl.setOnClickListener(v -> checkAndStartVoice());
        }
        if (cardVoiceCommand != null) {
            cardVoiceCommand.setOnClickListener(v -> checkAndStartVoice());
        }

        // Notification bell -> NotificationsActivity
        View btnNotification = view.findViewById(R.id.btn_notification);
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), NotificationsActivity.class)));
        }

        // History icon -> My Reports
        View btnHistory = view.findViewById(R.id.btn_history);
        if (btnHistory != null) {
            btnHistory.setOnClickListener(v -> openMyReports());
        }

        // Load database stats
        loadLiveStats();
        observeUnreadNotifications(view);
        seedDemoNotificationsIfNeeded();

        // Category click listeners -> open Report Creation prefilled
        view.findViewById(R.id.cat_waste).setOnClickListener(v -> openCreateReport("WASTE"));
        view.findViewById(R.id.cat_water).setOnClickListener(v -> openCreateReport("WATER"));
        view.findViewById(R.id.cat_air).setOnClickListener(v -> openCreateReport("AIR"));
        view.findViewById(R.id.cat_green_spaces).setOnClickListener(v -> openCreateReport("GREEN_SPACES"));
        View catOther = view.findViewById(R.id.cat_other);
        if (catOther != null) {
            catOther.setOnClickListener(v -> openCreateReport("OTHER"));
        }

        // Report CTA
        view.findViewById(R.id.card_report_cta).setOnClickListener(v -> openCreateReport(null));

        // Recent report card click
        cardRecentReport.setOnClickListener(v -> {
            if (latestReportId != null) {
                Intent intent = new Intent(requireContext(), ReportDetailActivity.class);
                intent.putExtra(ReportDetailActivity.EXTRA_REPORT_ID, latestReportId);
                startActivity(intent);
            } else {
                openMyReports();
            }
        });
    }

    // ─── Voice Automation ─────────────────────────────────────────────────────

    private void checkAndStartVoice() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            launchSpeechRecognizer();
        } else {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void launchSpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-IN,hi-IN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say 'File a report', 'Community', 'My reports'...");
        try {
            voiceControlLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Voice input not supported on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    private void processVoiceCommand(String rawQuery) {
        if (rawQuery == null) return;
        String query = rawQuery.toLowerCase(java.util.Locale.ROOT).trim();

        // 0. Stop listening
        if (query.contains("stop") || query.contains("ruko") || query.contains("ruk jao") || query.contains("cancel") || query.contains("band karo")) {
            Toast.makeText(requireContext(), "Voice listening stopped.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Water reporting
        if (query.contains("water leak") || query.contains("pani leak") || (query.contains("water") && (query.contains("leak") || query.contains("pipe") || query.contains("drain")))) {
            Toast.makeText(requireContext(), "Opening report for Water Leakage. Please fill details and confirm.", Toast.LENGTH_LONG).show();
            openCreateReport("WATER", "Water Leakage Issue");
            return;
        }

        // 2. Waste reporting
        if (query.contains("garbage near my house") || query.contains("kachre ki complaint") || query.contains("kachra") || query.contains("garbage") || query.contains("waste") || query.contains("dump")) {
            Toast.makeText(requireContext(), "Opening report for Waste / Garbage. Please fill details and confirm.", Toast.LENGTH_LONG).show();
            openCreateReport("WASTE", "Garbage / Waste Issue");
            return;
        }

        // 3. Air reporting
        if (query.contains("air pollution") || query.contains("hawa pradushan") || query.contains("smoke") || query.contains("dhuan") || query.contains("smog")) {
            Toast.makeText(requireContext(), "Opening report for Air Pollution. Please fill details and confirm.", Toast.LENGTH_LONG).show();
            openCreateReport("AIR", "Air Pollution Issue");
            return;
        }

        // 4. Tree / Green reporting
        if (query.contains("tree cutting") || query.contains("ped katna") || query.contains("tree") || query.contains("ped") || query.contains("green space")) {
            Toast.makeText(requireContext(), "Opening report for Green Spaces. Please fill details and confirm.", Toast.LENGTH_LONG).show();
            openCreateReport("GREEN_SPACES", "Tree / Green Space Issue");
            return;
        }

        // 5. General Report / Complaint / Issue
        if (query.contains("report") || query.contains("file") || query.contains("complaint")
                || query.contains("issue") || query.contains("grievance") || query.contains("submit")
                || query.contains("problem") || query.contains("shikayat") || query.contains("darj")) {
            Toast.makeText(requireContext(), "Opening Complaint section. Please select category and fill details.", Toast.LENGTH_LONG).show();
            openCreateReport(null, null);
            return;
        }

        // 6. Community
        if (query.contains("community") || query.contains("feed") || query.contains("public")
                || query.contains("samuday") || query.contains("social") || query.contains("nearby")) {
            Toast.makeText(requireContext(), "Opening Community feed...", Toast.LENGTH_SHORT).show();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setSelectedTab(R.id.nav_community);
            } else if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new CommunityFragment())
                        .addToBackStack(null)
                        .commit();
            }
            return;
        }

        // 7. My Reports / Status / Tracking
        if (query.contains("my report") || query.contains("my reports") || query.contains("status")
                || query.contains("history") || query.contains("track") || query.contains("tracking")
                || query.contains("meri report")) {
            Toast.makeText(requireContext(), "Opening My Reports...", Toast.LENGTH_SHORT).show();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setSelectedTab(R.id.nav_my_reports);
            } else {
                openMyReports();
            }
            return;
        }

        // 8. Impact / Analytics
        if (query.contains("impact") || query.contains("analytic") || query.contains("stat")
                || query.contains("metric") || query.contains("prabhav") || query.contains("progress")) {
            Toast.makeText(requireContext(), "Opening Environmental Impact...", Toast.LENGTH_SHORT).show();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setSelectedTab(R.id.nav_impact);
            } else if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ImpactFragment())
                        .addToBackStack(null)
                        .commit();
            }
            return;
        }

        // 9. Profile / Settings / Roles
        if (query.contains("profile") || query.contains("account") || query.contains("setting")
                || query.contains("role") || query.contains("admin") || query.contains("worker")) {
            Toast.makeText(requireContext(), "Opening Profile...", Toast.LENGTH_SHORT).show();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setSelectedTab(R.id.nav_profile);
            } else if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment())
                        .addToBackStack(null)
                        .commit();
            }
            return;
        }

        // 10. Notifications / Alerts
        if (query.contains("notif") || query.contains("alert") || query.contains("update") || query.contains("sandesh")) {
            Toast.makeText(requireContext(), "Opening Notifications...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireContext(), NotificationsActivity.class));
            return;
        }

        // 11. Fallback to Eco-Assistant
        Toast.makeText(requireContext(), "Asking Eco-Assistant...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(requireContext(), AssistantActivity.class);
        intent.putExtra("extra_initial_query", rawQuery);
        startActivity(intent);
    }

    private void observeUnreadNotifications(View view) {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        db.notificationDao().getUnreadCount().observe(getViewLifecycleOwner(), count -> {
            View badgeView = view.findViewById(R.id.view_notif_unread_badge);
            if (badgeView != null) {
                badgeView.setVisibility(count != null && count > 0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void seedDemoNotificationsIfNeeded() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        Executors.newSingleThreadExecutor().execute(() -> {
            if (db.notificationDao().count() == 0) {
                long now = System.currentTimeMillis();

                NotificationEntity n1 = new NotificationEntity();
                n1.title = "Welcome to SUSTAINIQ";
                n1.message = "Start by reporting an environmental issue in your area. Your voice matters!";
                n1.type = "SYSTEM";
                n1.isRead = false;
                n1.createdAt = now - 3600000L;
                db.notificationDao().insert(n1);

                NotificationEntity n2 = new NotificationEntity();
                n2.title = "Report Status Updated";
                n2.message = "Your report 'Overflowing garbage bin at FC Road' has been Triaged by municipal admin.";
                n2.type = "REPORT_STATUS";
                n2.isRead = false;
                n2.createdAt = now - 7200000L;
                db.notificationDao().insert(n2);

                NotificationEntity n3 = new NotificationEntity();
                n3.title = "Community Alert";
                n3.message = "Air quality in Pune has dropped to Unhealthy levels. Avoid outdoor activity.";
                n3.type = "ALERT";
                n3.isRead = false;
                n3.createdAt = now - 86400000L;
                db.notificationDao().insert(n3);

                NotificationEntity n4 = new NotificationEntity();
                n4.title = "Report Verified";
                n4.message = "Your water leakage report at Koregaon Park has been verified and resolved.";
                n4.type = "REPORT_STATUS";
                n4.isRead = true;
                n4.createdAt = now - 172800000L;
                db.notificationDao().insert(n4);
            }
        });
    }

    private void openCreateReport(String category) {
        openCreateReport(category, null);
    }

    private void openCreateReport(String category, String initialTitle) {
        if (getActivity() != null) {
            Fragment f = CreateReportFragment.newInstance(category, initialTitle);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, f)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void openMyReports() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MyReportsFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void loadLiveStats() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        db.reportDao().getAllReports().observe(getViewLifecycleOwner(), reports -> {
            if (reports == null || reports.isEmpty()) return;

            int total = reports.size();
            int resolved = 0;
            int impact = 0;

            for (ReportEntity r : reports) {
                if ("VERIFIED".equalsIgnoreCase(r.status) || "SUBMITTED".equalsIgnoreCase(r.status)) {
                    resolved++;
                }
                impact += r.upvotes;
            }

            if (tvStatTotal != null) tvStatTotal.setText(String.valueOf(total));
            if (tvStatResolved != null) tvStatResolved.setText(String.valueOf(resolved));
            if (tvStatImpact != null) tvStatImpact.setText(String.valueOf(impact > 0 ? impact : 24));

            ReportEntity latest = reports.get(0);
            latestReportId = latest.reportId;

            if (tvRecentTitle != null) tvRecentTitle.setText(latest.title);
            if (tvRecentStatus != null) {
                tvRecentStatus.setText(latest.status != null ? latest.status.replace('_', ' ') : "Reported");
                applyRecentBadgeColor(latest.status);
            }
            if (tvRecentLocation != null) {
                String loc = (latest.locality != null && !latest.locality.isEmpty()) ? latest.locality + ", " : "";
                loc += (latest.city != null && !latest.city.isEmpty()) ? latest.city : "Maharashtra";
                tvRecentLocation.setText(" " + loc);
            }
        });
    }

    private void applyRecentBadgeColor(String status) {
        if (tvRecentStatus == null || status == null) return;
        switch (status) {
            case "VERIFIED":
                tvRecentStatus.setBackgroundResource(R.drawable.bg_badge_verified);
                tvRecentStatus.setTextColor(Color.parseColor("#2E7D32"));
                break;
            case "IN_PROGRESS":
            case "ASSIGNED":
                tvRecentStatus.setBackgroundResource(R.drawable.bg_badge_in_progress);
                tvRecentStatus.setTextColor(Color.parseColor("#E65100"));
                break;
            default:
                tvRecentStatus.setBackgroundResource(R.drawable.bg_badge_reported);
                tvRecentStatus.setTextColor(Color.parseColor("#4A5E4D"));
                break;
        }
    }
}
