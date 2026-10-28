package com.sustainiq.app.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class AppUtils {

    private static final AtomicInteger reportCounter = new AtomicInteger(1);

    /** Generate a unique report ID: SIQ-YYYYMMDD-XXXX */
    public static String generateReportId() {
        String date = new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
        int seq = reportCounter.getAndIncrement();
        return String.format(Locale.US, "SIQ-%s-%04d", date, seq);
    }

    /** SHA-256 hash of a password. Never store plain text. */
    public static String hashPassword(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(plain.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return plain; // fallback (shouldn't happen on Android)
        }
    }

    /** Human-readable relative time string */
    public static String relativeTime(long epochMs) {
        long diff = System.currentTimeMillis() - epochMs;
        long mins  = diff / 60_000;
        long hours = diff / 3_600_000;
        long days  = diff / 86_400_000;
        if (mins < 1)    return "Just now";
        if (mins < 60)   return mins + " min ago";
        if (hours < 24)  return hours + " hr ago";
        if (days == 1)   return "Yesterday";
        return days + " days ago";
    }

    /** Status badge color (resource color ID string - use in code) */
    public static int statusColor(String status) {
        if (status == null) return 0xFF9EB8A2;
        switch (status) {
            case "TRIAGED":    return 0xFF5BA3C9;
            case "ASSIGNED":   return 0xFF7B68EE;
            case "IN_PROGRESS":return 0xFFE8A24A;
            case "SUBMITTED":  return 0xFF6B9A74;
            case "VERIFIED":   return 0xFF3D8F59;
            case "REJECTED":   return 0xFFBA1A1A;
            case "ESCALATED":  return 0xFFFF5722;
            default:           return 0xFF9EB8A2; // REPORTED
        }
    }

    /** Category icon resource name */
    public static String categoryIcon(String category) {
        if (category == null) return "ic_leaf";
        switch (category) {
            case "WASTE":        return "ic_waste";
            case "WATER":        return "ic_water";
            case "AIR":          return "ic_air";
            case "GREEN_SPACES": return "ic_green_spaces";
            default:             return "ic_leaf";
        }
    }

    /** Category display name */
    public static String categoryName(String cat) {
        if (cat == null) return "Other";
        switch (cat) {
            case "WASTE":        return "Waste";
            case "WATER":        return "Water";
            case "AIR":          return "Air";
            case "GREEN_SPACES": return "Green Spaces";
            default:             return "Other";
        }
    }

    /** Status display name */
    public static String statusName(String s) {
        if (s == null) return "Reported";
        switch (s) {
            case "TRIAGED":     return "Triaged";
            case "ASSIGNED":    return "Assigned";
            case "IN_PROGRESS": return "In Progress";
            case "SUBMITTED":   return "Resolution Submitted";
            case "VERIFIED":    return "Verified";
            case "REJECTED":    return "Rejected";
            case "ESCALATED":   return "Escalated";
            default:            return "Reported";
        }
    }
}
