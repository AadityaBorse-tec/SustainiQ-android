package com.sustainiq.app.data.db;

import android.content.Context;
import java.util.concurrent.Executors;

public class DataSeeder {

    public static void seedIfEmpty(Context context) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            long now = System.currentTimeMillis();
            long dayMs = 86400000L;

            // Seed primary citizen user if empty
            if (db.userDao().findByEmail("aaditya.borse@example.com") == null) {
                UserEntity u1 = new UserEntity();
                u1.id = 1;
                u1.name = "Aaditya Borse";
                u1.email = "aaditya.borse@example.com";
                u1.phone = "9876543210";
                u1.passwordHash = com.sustainiq.app.util.AppUtils.hashPassword("password123");
                u1.role = "CITIZEN";
                u1.savedCity = "Pune";
                u1.savedDistrict = "Pune";
                u1.savedPinCode = "411004";
                u1.language = "en";
                u1.createdAt = now;
                u1.lastLoginAt = now;
                db.userDao().insertUser(u1);
            }

            // Seed field worker
            if (db.userDao().findByEmail("worker.ward12@sustainiq.app") == null) {
                UserEntity w1 = new UserEntity();
                w1.id = 101;
                w1.name = "Ramesh Shinde";
                w1.email = "worker.ward12@sustainiq.app";
                w1.phone = "9822012345";
                w1.passwordHash = com.sustainiq.app.util.AppUtils.hashPassword("worker123");
                w1.role = "FIELD_WORKER";
                w1.savedCity = "Pune";
                w1.savedDistrict = "Pune";
                w1.language = "en";
                w1.createdAt = now;
                w1.lastLoginAt = now;
                db.userDao().insertUser(w1);
            }

            // Seed reports if empty
            if (db.reportDao().totalReports() > 0) {
                return;
            }

            // Report 1: Pune Waste
            ReportEntity r1 = new ReportEntity();
            r1.reportId = "SIQ-20261028-0101";
            r1.authorUserId = 1;
            r1.authorName = "Aaditya Borse";
            r1.title = "Overflowing garbage dump at FC Road";
            r1.description = "Community dustbin overflowing for 4 days. Waste spilling onto the pedestrian sidewalk creating health hazards.";
            r1.category = "WASTE";
            r1.status = "IN_PROGRESS";
            r1.priority = "HIGH";
            r1.pinCode = "411004";
            r1.locality = "FC Road, Shivajinagar";
            r1.city = "Pune";
            r1.district = "Pune";
            r1.state = "Maharashtra";
            r1.latitude = 18.5204;
            r1.longitude = 73.8567;
            r1.submittedAt = now - (dayMs * 2);
            r1.updatedAt = now - 3600000;
            r1.isPublic = true;
            r1.upvotes = 18;
            r1.assignedDepartment = "PMC Solid Waste Management Dept";
            db.reportDao().insertReport(r1);

            insertHistory(db, r1.reportId, "Aaditya Borse", "CITIZEN", null, "REPORTED", "Issue reported by citizen", r1.submittedAt);
            insertHistory(db, r1.reportId, "Admin PMC", "ADMINISTRATOR", "REPORTED", "TRIAGED", "Verified location and severity", r1.submittedAt + 3600000);
            insertHistory(db, r1.reportId, "Admin PMC", "ADMINISTRATOR", "TRIAGED", "ASSIGNED", "Assigned to Ward 12 Sanitation Team", r1.submittedAt + 7200000);
            insertHistory(db, r1.reportId, "Ramesh Shinde", "FIELD_WORKER", "ASSIGNED", "IN_PROGRESS", "Compactor vehicle dispatched to site", r1.updatedAt);

            // Report 2: Mumbai Water
            ReportEntity r2 = new ReportEntity();
            r2.reportId = "SIQ-20261027-0202";
            r2.authorUserId = 1;
            r2.authorName = "Demo User";
            r2.title = "Chemical effluent discharge near Mithi River";
            r2.description = "Dark colored untreated industrial effluent entering Mithi river via storm drain near BKC connector.";
            r2.category = "WATER";
            r2.status = "VERIFIED";
            r2.priority = "CRITICAL";
            r2.pinCode = "400051";
            r2.locality = "Bandra Kurla Complex";
            r2.city = "Mumbai";
            r2.district = "Mumbai Suburban";
            r2.state = "Maharashtra";
            r2.latitude = 19.0600;
            r2.longitude = 72.8685;
            r2.submittedAt = now - (dayMs * 4);
            r2.updatedAt = now - dayMs;
            r2.resolvedAt = now - dayMs;
            r2.isPublic = true;
            r2.upvotes = 42;
            r2.assignedDepartment = "MPCB Regional Office";
            db.reportDao().insertReport(r2);

            insertHistory(db, r2.reportId, "Demo User", "CITIZEN", null, "REPORTED", "Reported by citizen", r2.submittedAt);
            insertHistory(db, r2.reportId, "Admin MPCB", "ADMINISTRATOR", "REPORTED", "ASSIGNED", "Assigned to environmental inspector", r2.submittedAt + 7200000);
            insertHistory(db, r2.reportId, "Inspector K. Patil", "FIELD_WORKER", "ASSIGNED", "IN_PROGRESS", "Water samples collected for lab analysis", r2.submittedAt + 18000000);
            insertHistory(db, r2.reportId, "Inspector K. Patil", "FIELD_WORKER", "IN_PROGRESS", "SUBMITTED", "Inflow pipe sealed; fine notice issued to offender", r2.resolvedAt - 3600000);
            insertHistory(db, r2.reportId, "Admin MPCB", "ADMINISTRATOR", "SUBMITTED", "VERIFIED", "Closure report accepted and site verified clean", r2.resolvedAt);

            // Report 3: Nagpur Air
            ReportEntity r3 = new ReportEntity();
            r3.reportId = "SIQ-20261026-0303";
            r3.authorUserId = 2;
            r3.authorName = "Priya Deshmukh";
            r3.title = "Illegal open plastic burning on vacant plot";
            r3.description = "Heavy toxic black smoke blowing into residential colony every evening around 6 PM.";
            r3.category = "AIR";
            r3.status = "REPORTED";
            r3.priority = "MEDIUM";
            r3.pinCode = "440001";
            r3.locality = "Civil Lines";
            r3.city = "Nagpur";
            r3.district = "Nagpur";
            r3.state = "Maharashtra";
            r3.latitude = 21.1458;
            r3.longitude = 79.0882;
            r3.submittedAt = now - (dayMs * 1);
            r3.updatedAt = now - (dayMs * 1);
            r3.isPublic = true;
            r3.upvotes = 9;
            db.reportDao().insertReport(r3);

            insertHistory(db, r3.reportId, "Priya Deshmukh", "CITIZEN", null, "REPORTED", "Issue logged by citizen with photos", r3.submittedAt);

            // Report 4: Nashik Green Spaces
            ReportEntity r4 = new ReportEntity();
            r4.reportId = "SIQ-20261025-0404";
            r4.authorUserId = 1;
            r4.authorName = "Demo User";
            r4.title = "Illegal tree felling in Godavari River park";
            r4.description = "Mature peepal and neem trees being cut without municipal tree authority permission.";
            r4.category = "GREEN_SPACES";
            r4.status = "ASSIGNED";
            r4.priority = "HIGH";
            r4.pinCode = "422001";
            r4.locality = "Panchavati Riverfront";
            r4.city = "Nashik";
            r4.district = "Nashik";
            r4.state = "Maharashtra";
            r4.latitude = 19.9975;
            r4.longitude = 73.7898;
            r4.submittedAt = now - (dayMs * 3);
            r4.updatedAt = now - (dayMs * 2);
            r4.isPublic = true;
            r4.upvotes = 25;
            r4.assignedDepartment = "NMC Garden & Tree Authority";
            db.reportDao().insertReport(r4);

            insertHistory(db, r4.reportId, "Demo User", "CITIZEN", null, "REPORTED", "Citizen report filed", r4.submittedAt);
            insertHistory(db, r4.reportId, "Admin NMC", "ADMINISTRATOR", "REPORTED", "ASSIGNED", "Stop-work notice team assigned", r4.updatedAt);

            // Report 5: Thane Potable Water
            ReportEntity r5 = new ReportEntity();
            r5.reportId = "SIQ-20261024-0505";
            r5.authorUserId = 3;
            r5.authorName = "Suresh Jadhav";
            r5.title = "Burst potable water main line flooding Naupada road";
            r5.description = "Massive water pipe rupture pouring drinking water into street for over 8 hours.";
            r5.category = "WATER";
            r5.status = "VERIFIED";
            r5.priority = "HIGH";
            r5.pinCode = "400601";
            r5.locality = "Naupada Main Market";
            r5.city = "Thane";
            r5.district = "Thane";
            r5.state = "Maharashtra";
            r5.latitude = 19.2183;
            r5.longitude = 72.9781;
            r5.submittedAt = now - (dayMs * 5);
            r5.updatedAt = now - (dayMs * 4);
            r5.resolvedAt = now - (dayMs * 4);
            r5.isPublic = true;
            r5.upvotes = 34;
            r5.assignedDepartment = "TMC Water Supply Dept";
            db.reportDao().insertReport(r5);

            insertHistory(db, r5.reportId, "Suresh Jadhav", "CITIZEN", null, "REPORTED", "Emergency pipeline burst report", r5.submittedAt);
            insertHistory(db, r5.reportId, "Admin TMC", "ADMINISTRATOR", "REPORTED", "ASSIGNED", "Emergency repair unit dispatched", r5.submittedAt + 1800000);
            insertHistory(db, r5.reportId, "Worker V. Mane", "FIELD_WORKER", "ASSIGNED", "IN_PROGRESS", "Valve isolated and new sleeve welded", r5.submittedAt + 7200000);
            insertHistory(db, r5.reportId, "Worker V. Mane", "FIELD_WORKER", "IN_PROGRESS", "SUBMITTED", "Repair pressure tested and road cleaned", r5.resolvedAt - 1800000);
            insertHistory(db, r5.reportId, "Admin TMC", "ADMINISTRATOR", "SUBMITTED", "VERIFIED", "Pipeline fully restored and normal supply resumed", r5.resolvedAt);
        });
    }

    private static void insertHistory(AppDatabase db, String reportId, String actorName,
                                      String actorRole, String prevStatus, String newStatus,
                                      String note, long timestamp) {
        StatusHistoryEntity h = new StatusHistoryEntity();
        h.reportId = reportId;
        h.actorName = actorName;
        h.actorRole = actorRole;
        h.prevStatus = prevStatus;
        h.newStatus = newStatus;
        h.note = note;
        h.timestamp = timestamp;
        db.statusHistoryDao().insert(h);
    }
}
