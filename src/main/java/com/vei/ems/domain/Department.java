package com.vei.ems.domain;

public enum Department {
    ENGINEERING("Engineering"),
    HR("Human Resources"),
    FINANCE("Finance"),
    MARKETING("Marketing"),
    SALES("Sales"),
    OPERATIONS("Operations"),
    SUPPORT("Customer Support");

    private final String displayName;

    Department(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
