package com.sustainiq.app.data.repository;

import android.content.Context;
import android.os.AsyncTask;

import com.sustainiq.app.data.db.AppDatabase;
import com.sustainiq.app.data.db.ReportDao;
import com.sustainiq.app.data.db.ReportEntity;
import com.sustainiq.app.data.db.StatusHistoryDao;
import com.sustainiq.app.data.db.StatusHistoryEntity;
import com.sustainiq.app.util.AppUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportRepository {

    private final ReportDao        reportDao;
    private final StatusHistoryDao historyDao;
    private final ExecutorService  executor = Executors.newSingleThreadExecutor();

    public ReportRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        reportDao  = db.reportDao();
        historyDao = db.statusHistoryDao();
    }

    /** Save a brand-new report and record its initial status history entry */
    public void submitReport(ReportEntity report, Runnable onSuccess, Runnable onError) {
        executor.execute(() -> {
            try {
                report.status      = "REPORTED";
                report.submittedAt = System.currentTimeMillis();
                report.updatedAt   = report.submittedAt;
                reportDao.insertReport(report);

                StatusHistoryEntity h = new StatusHistoryEntity();
                h.reportId  = report.reportId;
                h.actorName = report.authorName;
                h.actorRole = "CITIZEN";
                h.prevStatus = null;
                h.newStatus  = "REPORTED";
                h.note       = "Report submitted by citizen";
                h.timestamp  = report.submittedAt;
                historyDao.insert(h);

                if (onSuccess != null) onSuccess.run();
            } catch (Exception e) {
                if (onError != null) onError.run();
            }
        });
    }

    /** Admin/worker updates a report status */
    public void updateReportStatus(String reportId, String newStatus,
                                   String actorName, String actorRole, String note) {
        executor.execute(() -> {
            long now = System.currentTimeMillis();
            ReportEntity existing = reportDao.getReportByIdSync(reportId);
            if (existing == null) return;

            String prev = existing.status;
            reportDao.updateStatus(reportId, newStatus, now);

            StatusHistoryEntity h = new StatusHistoryEntity();
            h.reportId   = reportId;
            h.actorName  = actorName;
            h.actorRole  = actorRole;
            h.prevStatus = prev;
            h.newStatus  = newStatus;
            h.note       = note;
            h.timestamp  = now;
            historyDao.insert(h);
        });
    }

    public ReportDao getReportDao() { return reportDao; }
    public StatusHistoryDao getHistoryDao() { return historyDao; }
    public ExecutorService getExecutor() { return executor; }
}
