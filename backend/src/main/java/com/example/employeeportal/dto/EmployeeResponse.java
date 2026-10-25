package com.example.employeeportal.dto;

import com.example.employeeportal.entity.Employee;
import com.example.employeeportal.entity.EmploymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        String employeeCode,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phone,
        String designation,
        BigDecimal salary,
        LocalDate dateOfJoining,
        EmploymentStatus status,
        DepartmentResponse department,
        ProjectResponse project,
        Instant createdAt,
        Instant updatedAt
) {

    public static EmployeeResponse from(Employee employee) {
        DepartmentResponse department = employee.getDepartment() == null
                ? null
                : DepartmentResponse.from(employee.getDepartment());
        ProjectResponse project = employee.getProject() == null
                ? null
                : ProjectResponse.from(employee.getProject());

        return new EmployeeResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getFirstName() + " " + employee.getLastName(),
                employee.getEmail(),
                employee.getPhone(),
                employee.getDesignation(),
                employee.getSalary(),
                employee.getDateOfJoining(),
                employee.getStatus(),
                department,
                project,
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }
}
