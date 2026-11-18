package com.vei.ems.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** One line of the activity log: who did what, and when. */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt;

    @Column(nullable = false, length = 30)
    private String username;

    @Column(nullable = false, length = 30)
    private String action;

    @Column(length = 255)
    private String details;

    public AuditLog() {
        // required by JPA
    }

    public AuditLog(String username, String action, String details) {
        this.loggedAt = LocalDateTime.now();
        this.username = username;
        this.action = action;
        this.details = details;
    }

    public Long getId() { return id; }
    public LocalDateTime getLoggedAt() { return loggedAt; }
    public String getUsername() { return username; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
}
