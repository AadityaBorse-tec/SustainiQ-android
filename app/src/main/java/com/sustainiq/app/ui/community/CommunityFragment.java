package com.sustainiq.app.ui.community;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.sustainiq.app.R;
import com.sustainiq.app.data.db.AppDatabase;
import com.sustainiq.app.data.db.ReportEntity;
import com.sustainiq.app.ui.reports.ReportAdapter;
import com.sustainiq.app.ui.reports.ReportDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class CommunityFragment extends Fragment implements ReportAdapter.OnReportClickListener {

    private RecyclerView rvCommunity;
    private ReportAdapter adapter;
    private Chip chipAll, chipWaste, chipWater, chipAir, chipGreen;

    private List<ReportEntity> allPublicReports = new ArrayList<>();
    private String selectedCategory = "ALL";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCommunity = view.findViewById(R.id.rv_community_reports);
        chipAll     = view.findViewById(R.id.chip_comm_all);
        chipWaste   = view.findViewById(R.id.chip_comm_waste);
        chipWater   = view.findViewById(R.id.chip_comm_water);
        chipAir     = view.findViewById(R.id.chip_comm_air);
        chipGreen   = view.findViewById(R.id.chip_comm_green);

        adapter = new ReportAdapter(this);
        rvCommunity.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCommunity.setAdapter(adapter);

        setupChips();
        observeCommunityReports();
    }

    private void setupChips() {
        chipAll.setOnClickListener(v -> { selectedCategory = "ALL"; applyFilter(); });
        chipWaste.setOnClickListener(v -> { selectedCategory = "WASTE"; applyFilter(); });
        chipWater.setOnClickListener(v -> { selectedCategory = "WATER"; applyFilter(); });
        chipAir.setOnClickListener(v -> { selectedCategory = "AIR"; applyFilter(); });
        chipGreen.setOnClickListener(v -> { selectedCategory = "GREEN_SPACES"; applyFilter(); });
    }

    private void observeCommunityReports() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        db.reportDao().getPublicReports().observe(getViewLifecycleOwner(), reports -> {
            allPublicReports = reports != null ? reports : new ArrayList<>();
            applyFilter();
        });
    }

    private void applyFilter() {
        List<ReportEntity> filtered = new ArrayList<>();
        for (ReportEntity r : allPublicReports) {
            if ("ALL".equals(selectedCategory) || selectedCategory.equalsIgnoreCase(r.category)) {
                filtered.add(r);
            }
        }
        adapter.setReports(filtered);
    }

    @Override
    public void onReportClick(ReportEntity report) {
        Intent intent = new Intent(requireContext(), ReportDetailActivity.class);
        intent.putExtra(ReportDetailActivity.EXTRA_REPORT_ID, report.reportId);
        startActivity(intent);
    }
}
