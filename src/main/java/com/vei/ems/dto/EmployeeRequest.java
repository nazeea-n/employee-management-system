package com.vei.ems.dto;

import com.vei.ems.domain.Department;
import com.vei.ems.domain.EmployeeType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** The JSON a client sends to create or update an employee. */
public record EmployeeRequest(
        @NotBlank(message = "Name is required")
        @Pattern(regexp = "^[A-Za-z][A-Za-z .'-]{1,49}$", message = "Name must be 2-50 letters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        @Size(max = 100, message = "Email is too long")
        String email,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^\\d{10}$", message = "Phone must be a 10 digit number")
        String phone,

        @NotNull(message = "Department is required") Department department,

        @NotBlank(message = "Designation is required")
        @Pattern(regexp = "^[A-Za-z][A-Za-z &.-]{1,39}$", message = "Designation must be 2-40 letters")
        String designation,

        @NotNull(message = "Joining date is required")
        @PastOrPresent(message = "Joining date cannot be in the future")
        LocalDate joiningDate,

        @NotNull(message = "Employee type is required") EmployeeType type,

        Double basicSalary,

        Double hourlyRate,

        @Min(value = 1, message = "Hours worked must be between 1 and 300")
        @Max(value = 300, message = "Hours worked must be between 1 and 300")
        Integer hoursWorked) {
}
