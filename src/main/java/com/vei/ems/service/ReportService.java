package com.vei.ems.service;

import com.vei.ems.domain.Department;
import com.vei.ems.domain.Employee;
import com.vei.ems.dto.ReportSummary;
import com.vei.ems.repository.EmployeeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Builds the numbers for the Reports page using Java Streams. */
@Service
public class ReportService {

    private final EmployeeRepository repository;

    public ReportService(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ReportSummary summary() {
        List<Employee> all = repository.findAll();

        double total = all.stream().mapToDouble(EmployeeService::monthlyPay).sum();
        Employee top = all.stream()
                .max(Comparator.comparingDouble(EmployeeService::monthlyPay))
                .orElse(null);

        Map<Department, List<Employee>> byDepartment = all.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment, TreeMap::new, Collectors.toList()));

        List<ReportSummary.DepartmentStat> stats = byDepartment.entrySet().stream()
                .map(entry -> {
                    double sum = entry.getValue().stream().mapToDouble(EmployeeService::monthlyPay).sum();
                    long count = entry.getValue().size();
                    return new ReportSummary.DepartmentStat(entry.getKey().name(),
                            entry.getKey().getDisplayName(), count,
                            PayCalculator.round(sum / count), PayCalculator.round(sum));
                })
                .collect(Collectors.toList());

        return new ReportSummary(all.size(), PayCalculator.round(total),
                top == null ? null : top.getName(),
                top == null ? null : top.getEmployeeCode(),
                top == null ? 0 : EmployeeService.monthlyPay(top),
                stats);
    }

    /** The whole report as CSV text (opens in Excel): summary, department table and employee list. */
    @Transactional(readOnly = true)
    public String exportCsv() {
        List<Employee> all = repository.findAll(Sort.by("id"));
        ReportSummary summary = summary();
        StringBuilder sb = new StringBuilder();

        sb.append(CsvUtil.row("EMPLOYEE MANAGEMENT SYSTEM - REPORT")).append('\n');
        sb.append(CsvUtil.row("Generated on",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))).append('\n');
        sb.append('\n');
        sb.append(CsvUtil.row("Total employees", summary.totalEmployees())).append('\n');
        sb.append(CsvUtil.row("Total monthly payroll", money(summary.totalMonthlyPayroll()))).append('\n');
        if (summary.highestPaidName() != null) {
            sb.append(CsvUtil.row("Highest paid", summary.highestPaidName() + " (" + summary.highestPaidCode() + ")",
                    money(summary.highestPaidAmount()))).append('\n');
        }

        sb.append('\n');
        sb.append(CsvUtil.row("DEPARTMENT SUMMARY")).append('\n');
        sb.append(CsvUtil.row("Department", "Employees", "Average pay", "Total pay")).append('\n');
        for (ReportSummary.DepartmentStat d : summary.departments()) {
            sb.append(CsvUtil.row(d.departmentName(), d.count(), money(d.averagePay()), money(d.totalPay())))
                    .append('\n');
        }

        sb.append('\n');
        sb.append(CsvUtil.row("EMPLOYEE LIST")).append('\n');
        sb.append(CsvUtil.row("ID", "Name", "Type", "Department", "Designation", "Email", "Phone", "Joined",
                "Monthly pay")).append('\n');
        for (Employee e : all) {
            sb.append(CsvUtil.row(e.getEmployeeCode(), e.getName(), e.getType().name(),
                    e.getDepartment().getDisplayName(), e.getDesignation(), e.getEmail(), e.getPhone(),
                    e.getJoiningDate(), money(EmployeeService.monthlyPay(e)))).append('\n');
        }
        return sb.toString();
    }

    private static String money(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
