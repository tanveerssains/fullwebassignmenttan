package com.example.employeeportal.dto;

import com.example.employeeportal.entity.Project;

public record ProjectResponse(Long id, String name, String client, String status) {

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(project.getId(), project.getName(), project.getClient(), project.getStatus());
    }
}
