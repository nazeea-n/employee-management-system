package com.vei.ems.dto;

import java.util.List;

public record PayslipResponse(
        String employeeCode,
        String name,
        String department,
        String designation,
        String type,
        List<Line> lines,
        double netSalary) {

    /** One row of the payslip, for example "HRA (20%)" and its amount. */
    public record Line(String label, double amount) {
    }
}
