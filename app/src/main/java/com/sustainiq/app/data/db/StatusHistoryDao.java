package com.sustainiq.app.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface StatusHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StatusHistoryEntity entry);

    @Query("SELECT * FROM status_history WHERE reportId = :reportId ORDER BY timestamp ASC")
    LiveData<List<StatusHistoryEntity>> getHistoryForReport(String reportId);

    @Query("SELECT * FROM status_history WHERE reportId = :reportId ORDER BY timestamp ASC")
    List<StatusHistoryEntity> getHistorySync(String reportId);
}
