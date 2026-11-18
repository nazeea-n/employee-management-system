package com.vei.ems.web;

import com.vei.ems.dto.ReportSummary;
import com.vei.ems.service.AuditService;
import com.vei.ems.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final AuditService auditService;

    public ReportController(ReportService reportService, AuditService auditService) {
        this.reportService = reportService;
        this.auditService = auditService;
    }

    @GetMapping("/summary")
    public ReportSummary summary() {
        return reportService.summary();
    }

    /** Downloads the full report as a CSV file (opens in Excel). */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(Authentication authentication) {
        // The leading \uFEFF tells Excel that the file is UTF-8
        byte[] body = ("\uFEFF" + reportService.exportCsv()).getBytes(StandardCharsets.UTF_8);
        auditService.log(authentication.getName(), "EXPORT_REPORT", "csv");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"employee-report.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }
}
