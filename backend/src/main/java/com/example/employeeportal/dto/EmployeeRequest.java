package com.example.employeeportal.dto;

import com.example.employeeportal.entity.EmploymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeRequest(
        @NotBlank(message = "Employee code is required")
        @Size(max = 20, message = "Employee code must be at most 20 characters")
        @Pattern(regexp = "^[A-Za-z0-9-]*$", message = "Employee code may contain letters, numbers and hyphens only")
        String employeeCode,

        @NotBlank(message = "First name is required")
        @Size(max = 60, message = "First name must be at most 60 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 60, message = "Last name must be at most 60 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        @Size(max = 150, message = "Email must be at most 150 characters")
        String email,

        @Pattern(regexp = "^$|^[0-9+()\\-\\s]{7,20}$", message = "Enter a valid phone number")
        String phone,

        @NotBlank(message = "Designation is required")
        @Size(max = 100, message = "Designation must be at most 100 characters")
        String designation,

        @NotNull(message = "Salary is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be greater than zero")
        @Digits(integer = 10, fraction = 2, message = "Salary must have at most 10 digits and 2 decimals")
        BigDecimal salary,

        @NotNull(message = "Date of joining is required")
        @PastOrPresent(message = "Date of joining cannot be in the future")
        LocalDate dateOfJoining,

        @NotNull(message = "Status is required")
        EmploymentStatus status,

        @NotNull(message = "Department is required")
        Long departmentId,

        Long projectId
) {
}
