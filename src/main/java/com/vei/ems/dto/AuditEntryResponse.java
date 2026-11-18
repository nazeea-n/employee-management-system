package com.vei.ems.dto;

import java.time.LocalDateTime;

public record AuditEntryResponse(LocalDateTime time, String username, String action, String details) {
}
