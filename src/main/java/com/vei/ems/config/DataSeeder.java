package com.vei.ems.config;

import com.vei.ems.domain.Department;
import com.vei.ems.domain.EmployeeType;
import com.vei.ems.dto.EmployeeRequest;
import com.vei.ems.repository.EmployeeRepository;
import com.vei.ems.service.AuthService;
import com.vei.ems.service.EmployeeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/** Runs once at start-up: makes sure an admin exists and, optionally, adds a few sample employees. */
@Component
public class DataSeeder implements CommandLineRunner {

    private final AuthService authService;
    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;
    private final String adminPassword;
    private final boolean seedSampleData;

    public DataSeeder(AuthService authService, EmployeeService employeeService,
                      EmployeeRepository employeeRepository,
                      @Value("${app.admin.password}") String adminPassword,
                      @Value("${app.seed-sample-data}") boolean seedSampleData) {
        this.authService = authService;
        this.employeeService = employeeService;
        this.employeeRepository = employeeRepository;
        this.adminPassword = adminPassword;
        this.seedSampleData = seedSampleData;
    }

    @Override
    public void run(String... args) {
        authService.createDefaultAdminIfNoUsers(adminPassword);

        if (seedSampleData && employeeRepository.count() == 0) {
            employeeService.create(fullTime("Peter Parker", "peter.parker.demo@gmail.com", "9000000001",
                    Department.ENGINEERING, "Software Developer", LocalDate.of(2022, 3, 14), 60000.0));
            employeeService.create(fullTime("Gwen Stacy", "gwen.stacy.demo@gmail.com", "9000000002",
                    Department.HR, "HR Manager", LocalDate.of(2021, 8, 1), 52000.0));
            employeeService.create(fullTime("Harry Potter", "harry.potter.demo@gmail.com", "9000000003",
                    Department.OPERATIONS, "Operations Executive", LocalDate.of(2023, 5, 22), 38000.0));
            employeeService.create(fullTime("Dustin", "dustin.demo@gmail.com", "9000000004",
                    Department.FINANCE, "Accountant", LocalDate.of(2023, 1, 9), 40000.0));
            employeeService.create(new EmployeeRequest("Will", "will.demo@gmail.com", "9000000005",
                    Department.MARKETING, "Content Writer", LocalDate.of(2024, 6, 17),
                    EmployeeType.PART_TIME, null, 350.0, 80));
        }
    }

    private EmployeeRequest fullTime(String name, String email, String phone, Department department,
                                     String designation, LocalDate joined, double basic) {
        return new EmployeeRequest(name, email, phone, department, designation, joined,
                EmployeeType.FULL_TIME, basic, null, null);
    }
}
