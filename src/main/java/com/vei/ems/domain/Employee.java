package com.vei.ems.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/** An employee row in the database. Pay fields depend on the employee type. */
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_code", unique = true, length = 10)
    private String employeeCode;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 10)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Department department;

    @Column(nullable = false, length = 40)
    private String designation;

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_type", nullable = false, length = 10)
    private EmployeeType type;

    @Column(name = "basic_salary")
    private Double basicSalary;      // full-time only

    @Column(name = "hourly_rate")
    private Double hourlyRate;       // part-time only

    @Column(name = "hours_worked")
    private Integer hoursWorked;     // part-time only

    public Employee() {
        // required by JPA
    }

    public Long getId() { return id; }
    public String getEmployeeCode() { return employeeCode; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Department getDepartment() { return department; }
    public String getDesignation() { return designation; }
    public LocalDate getJoiningDate() { return joiningDate; }
    public EmployeeType getType() { return type; }
    public Double getBasicSalary() { return basicSalary; }
    public Double getHourlyRate() { return hourlyRate; }
    public Integer getHoursWorked() { return hoursWorked; }

    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setDepartment(Department department) { this.department = department; }
    public void setDesignation(String designation) { this.designation = designation; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }
    public void setType(EmployeeType type) { this.type = type; }
    public void setBasicSalary(Double basicSalary) { this.basicSalary = basicSalary; }
    public void setHourlyRate(Double hourlyRate) { this.hourlyRate = hourlyRate; }
    public void setHoursWorked(Integer hoursWorked) { this.hoursWorked = hoursWorked; }
}
