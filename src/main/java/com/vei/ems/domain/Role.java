package com.vei.ems.domain;

/** What a logged-in user is allowed to do. */
public enum Role {
    ADMIN,    // everything, including user management and the activity log
    HR,       // add, edit and delete employees
    VIEWER    // read-only
}
