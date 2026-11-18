package com.vei.ems.dto;

import com.vei.ems.domain.Department;
import com.vei.ems.domain.EmployeeType;

import java.time.LocalDate;

/** The JSON sent back to the client (never the raw database entity). */
public record EmployeeResponse(
        Long id,
        String employeeCode,
        String name,
        String email,
        String phone,
        Department department,
        String departmentName,
        String designation,
        LocalDate joiningDate,
        EmployeeType type,
        Double basicSalary,
        Double hourlyRate,
        Integer hoursWorked,
        double monthlyPay) {
}
