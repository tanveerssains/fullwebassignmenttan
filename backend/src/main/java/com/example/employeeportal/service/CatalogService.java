package com.example.employeeportal.service;

import com.example.employeeportal.dto.DepartmentResponse;
import com.example.employeeportal.dto.ProjectResponse;
import com.example.employeeportal.repository.DepartmentRepository;
import com.example.employeeportal.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final DepartmentRepository departmentRepository;
    private final ProjectRepository projectRepository;

    public CatalogService(DepartmentRepository departmentRepository, ProjectRepository projectRepository) {
        this.departmentRepository = departmentRepository;
        this.projectRepository = projectRepository;
    }

    public List<DepartmentResponse> findDepartments() {
        return departmentRepository.findAllByOrderByNameAsc().stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    public List<ProjectResponse> findProjects() {
        return projectRepository.findAllByOrderByNameAsc().stream()
                .map(ProjectResponse::from)
                .toList();
    }
}
