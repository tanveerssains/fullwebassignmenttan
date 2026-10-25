package com.example.employeeportal.controller;

import com.example.employeeportal.dto.DepartmentResponse;
import com.example.employeeportal.dto.ProjectResponse;
import com.example.employeeportal.service.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/departments")
    public List<DepartmentResponse> findDepartments() {
        return catalogService.findDepartments();
    }

    @GetMapping("/projects")
    public List<ProjectResponse> findProjects() {
        return catalogService.findProjects();
    }
}
