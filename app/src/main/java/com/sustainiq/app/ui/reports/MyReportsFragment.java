package com.sustainiq.app.ui.reports;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.sustainiq.app.R;
import com.sustainiq.app.data.db.AppDatabase;
import com.sustainiq.app.data.db.ReportEntity;

import java.util.ArrayList;
import java.util.List;

public class MyReportsFragment extends Fragment implements ReportAdapter.OnReportClickListener {

    private RecyclerView rvReports;
    private ReportAdapter adapter;
    private LinearLayout layoutEmpty;
    private TextInputEditText etSearch;
    private ChipGroup chipGroupStatus;
    private Chip chipAll, chipReported, chipProgress, chipVerified;
    private ExtendedFloatingActionButton fabNewReport;

    private List<ReportEntity> allReportsList = new ArrayList<>();
    private String currentStatusFilter = "ALL";
    private String currentSearchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setupRecyclerView();
        setupFilters();
        setupSearch();
        setupFab(view);
        observeReports();
    }

    private void bindViews(View v) {
        rvReports       = v.findViewById(R.id.rv_reports);
        layoutEmpty     = v.findViewById(R.id.layout_empty);
        etSearch        = v.findViewById(R.id.et_search);
        chipGroupStatus = v.findViewById(R.id.chip_group_status);
        chipAll         = v.findViewById(R.id.chip_filter_all);
        chipReported    = v.findViewById(R.id.chip_filter_reported);
        chipProgress    = v.findViewById(R.id.chip_filter_progress);
        chipVerified    = v.findViewById(R.id.chip_filter_verified);
        fabNewReport    = v.findViewById(R.id.fab_new_report);
    }

    private void setupRecyclerView() {
        adapter = new ReportAdapter(this);
        rvReports.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvReports.setAdapter(adapter);
    }

    private void setupFilters() {
        chipAll.setOnClickListener(v -> {
            currentStatusFilter = "ALL";
            applyFilterAndSearch();
        });
        chipReported.setOnClickListener(v -> {
            currentStatusFilter = "REPORTED";
            applyFilterAndSearch();
        });
        chipProgress.setOnClickListener(v -> {
            currentStatusFilter = "IN_PROGRESS";
            applyFilterAndSearch();
        });
        chipVerified.setOnClickListener(v -> {
            currentStatusFilter = "VERIFIED";
            applyFilterAndSearch();
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                currentSearchQuery = s != null ? s.toString().trim().toLowerCase() : "";
                applyFilterAndSearch();
            }
        });
    }

    private void setupFab(View root) {
        fabNewReport.setOnClickListener(v -> openCreateReport(null));
        root.findViewById(R.id.btn_empty_create).setOnClickListener(v -> openCreateReport(null));
    }

    private void openCreateReport(String category) {
        if (getActivity() != null) {
            Fragment f = category != null ? CreateReportFragment.newInstance(category) : new CreateReportFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, f)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void observeReports() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        com.sustainiq.app.util.SessionManager session = new com.sustainiq.app.util.SessionManager(requireContext());
        int userId = session.getUserId();
        if (session.getUserRole() == com.sustainiq.app.data.model.UserRole.CITIZEN && userId > 0) {
            db.reportDao().getReportsByUser(userId).observe(getViewLifecycleOwner(), reports -> {
                allReportsList = reports != null ? reports : new ArrayList<>();
                applyFilterAndSearch();
            });
        } else {
            db.reportDao().getAllReports().observe(getViewLifecycleOwner(), reports -> {
                allReportsList = reports != null ? reports : new ArrayList<>();
                applyFilterAndSearch();
            });
        }
    }

    private void applyFilterAndSearch() {
        List<ReportEntity> filtered = new ArrayList<>();

        for (ReportEntity r : allReportsList) {
            // Status match
            boolean matchesStatus = true;
            if ("REPORTED".equals(currentStatusFilter)) {
                matchesStatus = "REPORTED".equalsIgnoreCase(r.status);
            } else if ("IN_PROGRESS".equals(currentStatusFilter)) {
                matchesStatus = "IN_PROGRESS".equalsIgnoreCase(r.status) || "ASSIGNED".equalsIgnoreCase(r.status) || "TRIAGED".equalsIgnoreCase(r.status);
            } else if ("VERIFIED".equals(currentStatusFilter)) {
                matchesStatus = "VERIFIED".equalsIgnoreCase(r.status) || "SUBMITTED".equalsIgnoreCase(r.status);
            }

            // Search query match
            boolean matchesSearch = true;
            if (!currentSearchQuery.isEmpty()) {
                String title = r.title != null ? r.title.toLowerCase() : "";
                String id = r.reportId != null ? r.reportId.toLowerCase() : "";
                String loc = r.city != null ? r.city.toLowerCase() : "";
                matchesSearch = title.contains(currentSearchQuery) || id.contains(currentSearchQuery) || loc.contains(currentSearchQuery);
            }

            if (matchesStatus && matchesSearch) {
                filtered.add(r);
            }
        }

        adapter.setReports(filtered);

        if (filtered.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvReports.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvReports.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onReportClick(ReportEntity report) {
        Intent intent = new Intent(requireContext(), ReportDetailActivity.class);
        intent.putExtra(ReportDetailActivity.EXTRA_REPORT_ID, report.reportId);
        startActivity(intent);
    }
}
