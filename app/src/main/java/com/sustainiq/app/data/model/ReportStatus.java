package com.sustainiq.app.data.model;

import com.sustainiq.app.R;

public enum ReportStatus {
    REPORTED("Reported", 1, R.color.status_reported, R.drawable.bg_badge_in_progress),
    TRIAGED("Triaged", 2, R.color.status_triaged, R.drawable.bg_badge_in_progress),
    ASSIGNED("Assigned", 3, R.color.status_assigned, R.drawable.bg_badge_in_progress),
    IN_PROGRESS("In Progress", 4, R.color.color_pending_amber, R.drawable.bg_badge_in_progress),
    RESOLUTION_SUBMITTED("Resolution Submitted", 5, R.color.status_submitted, R.drawable.bg_badge_in_progress),
    VERIFIED("Verified", 6, R.color.status_verified, R.drawable.bg_badge_verified),
    REJECTED("Rejected", -1, R.color.status_rejected, R.drawable.bg_badge_in_progress),
    ESCALATED("Escalated", -2, R.color.color_error, R.drawable.bg_badge_in_progress);

    private final String displayName;
    private final int stepOrder; // 1 to 6 in the verified pipeline
    private final int colorRes;
    private final int bgRes;

    ReportStatus(String displayName, int stepOrder, int colorRes, int bgRes) {
        this.displayName = displayName;
        this.stepOrder = stepOrder;
        this.colorRes = colorRes;
        this.bgRes = bgRes;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public int getColorRes() {
        return colorRes;
    }

    public int getBgRes() {
        return bgRes;
    }
}
