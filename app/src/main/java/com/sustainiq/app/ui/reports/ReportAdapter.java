package com.sustainiq.app.ui.reports;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sustainiq.app.R;
import com.sustainiq.app.data.db.ReportEntity;

import java.util.ArrayList;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    public interface OnReportClickListener {
        void onReportClick(ReportEntity report);
    }

    private final List<ReportEntity> reports = new ArrayList<>();
    private final OnReportClickListener listener;

    public ReportAdapter(OnReportClickListener listener) {
        this.listener = listener;
    }

    public void setReports(List<ReportEntity> newReports) {
        reports.clear();
        if (newReports != null) {
            reports.addAll(newReports);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReportEntity item = reports.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvId;
        private final TextView tvStatus;
        private final TextView tvCategory;
        private final TextView tvTitle;
        private final TextView tvDesc;
        private final TextView tvLocation;
        private final TextView tvDate;
        private final TextView tvUpvotes;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId       = itemView.findViewById(R.id.tv_item_id);
            tvStatus   = itemView.findViewById(R.id.tv_item_status);
            tvCategory = itemView.findViewById(R.id.tv_item_category);
            tvTitle    = itemView.findViewById(R.id.tv_item_title);
            tvDesc     = itemView.findViewById(R.id.tv_item_desc);
            tvLocation = itemView.findViewById(R.id.tv_item_location);
            tvDate     = itemView.findViewById(R.id.tv_item_date);
            tvUpvotes  = itemView.findViewById(R.id.tv_item_upvotes);
        }

        void bind(ReportEntity r, OnReportClickListener listener) {
            tvId.setText(r.reportId);
            tvTitle.setText(r.title);
            tvDesc.setText(r.description);

            // Category display
            String cat = r.category != null ? r.category.toUpperCase() : "OTHER";
            switch (cat) {
                case "WASTE":
                    tvCategory.setText("Waste Management");
                    tvCategory.setTextColor(Color.parseColor("#795548"));
                    break;
                case "WATER":
                    tvCategory.setText("Water Quality & Leaks");
                    tvCategory.setTextColor(Color.parseColor("#1565C0"));
                    break;
                case "AIR":
                    tvCategory.setText("Air & Pollution");
                    tvCategory.setTextColor(Color.parseColor("#546E7A"));
                    break;
                case "GREEN_SPACES":
                    tvCategory.setText("Parks & Greenery");
                    tvCategory.setTextColor(Color.parseColor("#2E7D32"));
                    break;
                default:
                    tvCategory.setText("General Issue");
                    tvCategory.setTextColor(Color.parseColor("#3D8F59"));
                    break;
            }

            // Status display
            String status = r.status != null ? r.status : "REPORTED";
            tvStatus.setText(status.replace('_', ' '));
            Context ctx = itemView.getContext();

            switch (status) {
                case "REPORTED":
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_reported);
                    tvStatus.setTextColor(Color.parseColor("#4A5E4D"));
                    break;
                case "TRIAGED":
                case "ASSIGNED":
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_assigned);
                    tvStatus.setTextColor(Color.parseColor("#5E35B1"));
                    break;
                case "IN_PROGRESS":
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_in_progress);
                    tvStatus.setTextColor(Color.parseColor("#E65100"));
                    break;
                case "VERIFIED":
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_verified);
                    tvStatus.setTextColor(Color.parseColor("#2E7D32"));
                    break;
                case "REJECTED":
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_rejected);
                    tvStatus.setTextColor(Color.parseColor("#C62828"));
                    break;
                default:
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_reported);
                    tvStatus.setTextColor(Color.parseColor("#4A5E4D"));
                    break;
            }

            // Location
            String loc = "";
            if (r.locality != null && !r.locality.isEmpty()) loc = r.locality + ", ";
            if (r.city != null && !r.city.isEmpty()) loc += r.city;
            else if (r.district != null && !r.district.isEmpty()) loc += r.district;
            else loc = "Maharashtra";
            tvLocation.setText(loc);

            // Date
            long diff = System.currentTimeMillis() - r.submittedAt;
            long hours = diff / (3600000L);
            long days = hours / 24;
            if (days > 0) {
                tvDate.setText(days + (days == 1 ? " day ago" : " days ago"));
            } else if (hours > 0) {
                tvDate.setText(hours + (hours == 1 ? " hr ago" : " hrs ago"));
            } else {
                tvDate.setText("Just now");
            }

            tvUpvotes.setText(String.valueOf(r.upvotes));

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onReportClick(r);
            });
        }
    }
}
