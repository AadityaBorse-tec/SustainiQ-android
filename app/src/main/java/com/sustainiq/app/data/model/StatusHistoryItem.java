package com.sustainiq.app.data.model;

import java.io.Serializable;

public class StatusHistoryItem implements Serializable {
    private ReportStatus status;
    private long timestamp;
    private String authorName;
    private String authorRole;
    private String notes;

    public StatusHistoryItem() {
    }

    public StatusHistoryItem(ReportStatus status, long timestamp, String authorName, String authorRole, String notes) {
        this.status = status;
        this.timestamp = timestamp;
        this.authorName = authorName;
        this.authorRole = authorRole;
        this.notes = notes;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorRole() {
        return authorRole;
    }

    public void setAuthorRole(String authorRole) {
        this.authorRole = authorRole;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
