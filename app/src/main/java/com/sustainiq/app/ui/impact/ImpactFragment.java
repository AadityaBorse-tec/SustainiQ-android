package com.sustainiq.app.ui.impact;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.sustainiq.app.R;
import com.sustainiq.app.data.db.AppDatabase;
import com.sustainiq.app.data.db.ReportEntity;

public class ImpactFragment extends Fragment {

    private TextView tvResolutionRate, tvRatio;
    private LinearProgressIndicator progressRate;
    private MaterialButton btnShare;

    private int totalReports = 0;
    private int verifiedReports = 0;
    private int resolutionPercent = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_impact, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvResolutionRate = view.findViewById(R.id.tv_impact_resolution_rate);
        tvRatio          = view.findViewById(R.id.tv_impact_ratio);
        progressRate     = view.findViewById(R.id.progress_impact_rate);
        btnShare         = view.findViewById(R.id.btn_share_impact);

        btnShare.setOnClickListener(v -> shareImpact());

        observeDatabaseImpact();
    }

    private void observeDatabaseImpact() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        db.reportDao().getAllReports().observe(getViewLifecycleOwner(), reports -> {
            if (reports == null || reports.isEmpty()) return;

            totalReports = reports.size();
            verifiedReports = 0;

            for (ReportEntity r : reports) {
                if ("VERIFIED".equalsIgnoreCase(r.status) || "SUBMITTED".equalsIgnoreCase(r.status)) {
                    verifiedReports++;
                }
            }

            resolutionPercent = totalReports > 0 ? (verifiedReports * 100) / totalReports : 0;

            if (tvResolutionRate != null) {
                tvResolutionRate.setText(resolutionPercent + "%");
            }
            if (tvRatio != null) {
                tvRatio.setText("  (" + verifiedReports + " of " + totalReports + " issues verified)");
            }
            if (progressRate != null) {
                progressRate.setProgress(resolutionPercent);
            }
        });
    }

    private void shareImpact() {
        String shareText = "SUSTAINIQ Environmental Impact Report — Maharashtra\n" +
                "• Civic Resolution Efficiency: " + resolutionPercent + "%\n" +
                "• Verified Issues Solved: " + verifiedReports + " of " + totalReports + "\n" +
                "• Waste Diverted: 4.8 Tons\n" +
                "• Potable Water Preserved: 1,200 Liters\n" +
                "• Trees Protected: 85+\n\n" +
                "Empowering civic accountability with SUSTAINIQ: Smarter Cities, Greener Tomorrow.";

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share SUSTAINIQ Impact");
        startActivity(shareIntent);
    }
}
