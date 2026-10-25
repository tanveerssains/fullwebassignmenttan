package com.example.employeeportal.dto;

import com.example.employeeportal.entity.Department;

public record DepartmentResponse(Long id, String name, String location) {

    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(department.getId(), department.getName(), department.getLocation());
    }
}
