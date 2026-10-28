package com.sustainiq.app.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import androidx.annotation.NonNull;

@Entity(tableName = "reports")
public class ReportEntity {
    @PrimaryKey
    @NonNull
    public String reportId = "";      // SIQ-YYYYMMDD-XXXX
    public int    authorUserId;
    public String authorName;
    public String title;
    public String description;
    public String category;      // WASTE | WATER | AIR | GREEN_SPACES | OTHER
    public String status;        // REPORTED | TRIAGED | ASSIGNED | IN_PROGRESS | SUBMITTED | VERIFIED | REJECTED | ESCALATED
    public String priority;      // LOW | MEDIUM | HIGH | CRITICAL
    public String photoUris;     // JSON array of local file URIs
    // Location
    public String pinCode;
    public String locality;
    public String city;
    public String district;
    public String state;
    public double latitude;
    public double longitude;
    // Assignment (admin use)
    public int    assignedToUserId;
    public String assignedDepartment;
    public String adminNotes;
    // Timestamps
    public long   submittedAt;
    public long   updatedAt;
    public long   resolvedAt;
    public boolean isPublic;
    public int    upvotes;
}
