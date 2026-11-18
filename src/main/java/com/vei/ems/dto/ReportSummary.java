package com.vei.ems.dto;

import java.util.List;

public record ReportSummary(
        long totalEmployees,
        double totalMonthlyPayroll,
        String highestPaidName,
        String highestPaidCode,
        double highestPaidAmount,
        List<DepartmentStat> departments) {

    public record DepartmentStat(String department, String departmentName, long count,
                                 double averagePay, double totalPay) {
    }
}
