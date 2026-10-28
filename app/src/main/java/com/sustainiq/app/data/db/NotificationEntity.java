package com.sustainiq.app.data.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications")
public class NotificationEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String title = "";

    @NonNull
    public String message = "";

    /** REPORT_STATUS | COMMUNITY | SYSTEM | ALERT */
    @NonNull
    public String type = "SYSTEM";

    /** Optional link: report ID to open on tap */
    public String relatedReportId;

    public boolean isRead = false;
    public long    createdAt;
}
