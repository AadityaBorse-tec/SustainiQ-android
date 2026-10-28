package com.sustainiq.app.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "status_history")
public class StatusHistoryEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String reportId;
    public String actorName;
    public String actorRole;
    public String prevStatus;
    public String newStatus;
    public String note;
    public long   timestamp;
}
