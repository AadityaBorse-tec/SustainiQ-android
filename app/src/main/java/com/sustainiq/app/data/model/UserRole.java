package com.sustainiq.app.data.model;

public enum UserRole {
    CITIZEN("Citizen", "Report issues, track status, view community impact"),
    ADMINISTRATOR("Administrator", "Review, assign workers, verify and escalate reports"),
    FIELD_WORKER("Field Worker", "View tasks, log progress, upload evidence and submit work");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
