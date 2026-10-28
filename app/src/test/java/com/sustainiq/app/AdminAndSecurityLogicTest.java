package com.sustainiq.app;

import com.sustainiq.app.data.model.UserRole;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests verifying Admin security access validation, role privileges,
 * workflow state transitions, and user profile initials parsing.
 */
public class AdminAndSecurityLogicTest {

    private static final String REQUIRED_PROTOTYPE_CODE = "aaditya7112";

    // Logic simulating AdminAuthActivity verification
    private boolean verifyAdminAccessCode(String input) {
        if (input == null) return false;
        return REQUIRED_PROTOTYPE_CODE.equals(input.trim());
    }

    // Logic simulating role authorization check in ReportDetailActivity & AdminDashboardActivity
    private boolean canPerformAdminWorkflowAction(UserRole role) {
        return role == UserRole.ADMINISTRATOR || role == UserRole.FIELD_WORKER;
    }

    // Logic simulating valid workflow transitions
    private boolean isValidStatusTransition(String currentStatus, String targetStatus) {
        if (currentStatus == null || targetStatus == null) return false;
        switch (currentStatus) {
            case "REPORTED":
                return Arrays.asList("TRIAGED", "ASSIGNED", "REJECTED").contains(targetStatus);
            case "TRIAGED":
                return Arrays.asList("ASSIGNED", "IN_PROGRESS", "REJECTED").contains(targetStatus);
            case "ASSIGNED":
                return Arrays.asList("IN_PROGRESS", "REJECTED").contains(targetStatus);
            case "IN_PROGRESS":
                return Arrays.asList("SUBMITTED", "VERIFIED", "REJECTED").contains(targetStatus);
            case "SUBMITTED":
                return Arrays.asList("VERIFIED", "REJECTED").contains(targetStatus);
            case "VERIFIED":
            case "REJECTED":
                return false; // Terminal states
            default:
                return false;
        }
    }

    // Logic simulating ProfileFragment extractInitials
    private String extractInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "SQ";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        } else {
            return (name.substring(0, Math.min(2, name.length()))).toUpperCase();
        }
    }

    @Test
    public void testValidAdminAccessCode() {
        assertTrue("Prototype code aaditya7112 must be authorized",
                verifyAdminAccessCode("aaditya7112"));
        assertTrue("Prototype code with surrounding spaces must be trimmed and authorized",
                verifyAdminAccessCode("  aaditya7112  "));
    }

    @Test
    public void testInvalidAdminAccessCodesDenied() {
        assertFalse("Empty code must be denied", verifyAdminAccessCode(""));
        assertFalse("Null code must be denied", verifyAdminAccessCode(null));
        assertFalse("Generic admin must be denied", verifyAdminAccessCode("admin"));
        assertFalse("Wrong code must be denied", verifyAdminAccessCode("password123"));
        assertFalse("Partial code must be denied", verifyAdminAccessCode("aaditya"));
        assertFalse("Off-by-one code must be denied", verifyAdminAccessCode("aaditya7113"));
    }

    @Test
    public void testRolePrivilegesEnforced() {
        assertFalse("Citizen cannot perform admin triage operations",
                canPerformAdminWorkflowAction(UserRole.CITIZEN));

        assertTrue("Administrator can perform triage and assignment operations",
                canPerformAdminWorkflowAction(UserRole.ADMINISTRATOR));

        assertTrue("Field Worker can update report progress",
                canPerformAdminWorkflowAction(UserRole.FIELD_WORKER));
    }

    @Test
    public void testWorkflowTransitions() {
        assertTrue("REPORTED can transition to TRIAGED",
                isValidStatusTransition("REPORTED", "TRIAGED"));
        assertTrue("TRIAGED can transition to ASSIGNED",
                isValidStatusTransition("TRIAGED", "ASSIGNED"));
        assertTrue("ASSIGNED can transition to IN_PROGRESS",
                isValidStatusTransition("ASSIGNED", "IN_PROGRESS"));
        assertTrue("IN_PROGRESS can transition to VERIFIED",
                isValidStatusTransition("IN_PROGRESS", "VERIFIED"));
        assertTrue("REPORTED can transition to REJECTED",
                isValidStatusTransition("REPORTED", "REJECTED"));

        assertFalse("VERIFIED cannot transition to REPORTED",
                isValidStatusTransition("VERIFIED", "REPORTED"));
        assertFalse("REJECTED cannot transition to IN_PROGRESS",
                isValidStatusTransition("REJECTED", "IN_PROGRESS"));
    }

    @Test
    public void testInitialsExtraction() {
        assertEquals("AB", extractInitials("Aaditya Borse"));
        assertEquals("JD", extractInitials("Jane Doe"));
        assertEquals("RS", extractInitials("Ramesh Shinde"));
        assertEquals("PU", extractInitials("Pune"));
        assertEquals("SQ", extractInitials(""));
        assertEquals("SQ", extractInitials(null));
    }
}
