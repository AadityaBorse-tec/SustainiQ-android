package com.sustainiq.app.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ReportDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReport(ReportEntity report);

    @Update
    void updateReport(ReportEntity report);

    // --- Citizen queries ---
    @Query("SELECT * FROM reports WHERE authorUserId = :userId ORDER BY submittedAt DESC")
    LiveData<List<ReportEntity>> getReportsByUser(int userId);

    @Query("SELECT * FROM reports WHERE authorUserId = :userId ORDER BY submittedAt DESC")
    List<ReportEntity> getReportsByUserSync(int userId);

    @Query("SELECT * FROM reports WHERE reportId = :id LIMIT 1")
    LiveData<ReportEntity> getReportById(String id);

    @Query("SELECT * FROM reports WHERE reportId = :id LIMIT 1")
    ReportEntity getReportByIdSync(String id);

    // --- Community ---
    @Query("SELECT * FROM reports WHERE isPublic = 1 ORDER BY submittedAt DESC LIMIT 50")
    LiveData<List<ReportEntity>> getPublicReports();

    @Query("SELECT * FROM reports WHERE isPublic = 1 AND category = :cat ORDER BY submittedAt DESC")
    LiveData<List<ReportEntity>> getPublicReportsByCategory(String cat);

    // --- Admin queries ---
    @Query("SELECT * FROM reports ORDER BY submittedAt DESC")
    LiveData<List<ReportEntity>> getAllReports();

    @Query("SELECT * FROM reports WHERE status = :status ORDER BY submittedAt DESC")
    LiveData<List<ReportEntity>> getReportsByStatus(String status);

    @Query("SELECT * FROM reports WHERE assignedToUserId = :workerId ORDER BY submittedAt DESC")
    LiveData<List<ReportEntity>> getReportsForWorker(int workerId);

    // --- Stats ---
    @Query("SELECT COUNT(*) FROM reports WHERE authorUserId = :userId")
    int countByUser(int userId);

    @Query("SELECT COUNT(*) FROM reports WHERE authorUserId = :userId AND status = 'VERIFIED'")
    int countResolvedByUser(int userId);

    @Query("SELECT COUNT(*) FROM reports")
    int totalReports();

    @Query("SELECT COUNT(*) FROM reports WHERE status = 'VERIFIED'")
    int totalVerified();

    @Query("SELECT COUNT(*) FROM reports WHERE status NOT IN ('VERIFIED','REJECTED')")
    int totalPending();

    @Query("SELECT COUNT(*) FROM reports WHERE category = :cat")
    int countByCategory(String cat);

    // --- Search ---
    @Query("SELECT * FROM reports WHERE authorUserId = :userId AND (title LIKE '%' || :query || '%' OR reportId LIKE '%' || :query || '%') ORDER BY submittedAt DESC")
    List<ReportEntity> searchReports(int userId, String query);

    // --- Status update ---
    @Query("UPDATE reports SET status = :status, updatedAt = :ts WHERE reportId = :id")
    void updateStatus(String id, String status, long ts);

    @Query("UPDATE reports SET upvotes = upvotes + 1 WHERE reportId = :id")
    void incrementUpvote(String id);
}
