package com.example.employeeportal.service;

import com.example.employeeportal.dto.EmployeeRequest;
import com.example.employeeportal.dto.EmployeeResponse;
import com.example.employeeportal.entity.Department;
import com.example.employeeportal.entity.Employee;
import com.example.employeeportal.entity.Project;
import com.example.employeeportal.exception.DuplicateResourceException;
import com.example.employeeportal.exception.ResourceNotFoundException;
import com.example.employeeportal.repository.DepartmentRepository;
import com.example.employeeportal.repository.EmployeeRepository;
import com.example.employeeportal.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final ProjectRepository projectRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           ProjectRepository projectRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll(String search) {
        String query = search == null ? "" : search.trim();
        return employeeRepository.search(query).stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) {
        return EmployeeResponse.from(getEmployee(id));
    }

    public EmployeeResponse create(EmployeeRequest request) {
        String employeeCode = normalizeCode(request.employeeCode());
        String email = normalizeEmail(request.email());

        if (employeeRepository.existsByEmployeeCodeIgnoreCase(employeeCode)) {
            throw new DuplicateResourceException("Employee code " + employeeCode + " is already in use.");
        }
        if (employeeRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email " + email + " is already in use.");
        }

        Employee employee = new Employee();
        applyRequest(employee, request, employeeCode, email);
        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = getEmployee(id);
        String employeeCode = normalizeCode(request.employeeCode());
        String email = normalizeEmail(request.email());

        if (employeeRepository.existsByEmployeeCodeIgnoreCaseAndIdNot(employeeCode, id)) {
            throw new DuplicateResourceException("Employee code " + employeeCode + " is already in use.");
        }
        if (employeeRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new DuplicateResourceException("Email " + email + " is already in use.");
        }

        applyRequest(employee, request, employeeCode, email);
        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    public void delete(Long id) {
        Employee employee = getEmployee(id);
        employeeRepository.delete(employee);
    }

    private Employee getEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee with id " + id + " was not found."));
    }

    private void applyRequest(Employee employee, EmployeeRequest request, String employeeCode, String email) {
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department with id " + request.departmentId() + " was not found."));

        Project project = null;
        if (request.projectId() != null) {
            project = projectRepository.findById(request.projectId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Project with id " + request.projectId() + " was not found."));
        }

        employee.setEmployeeCode(employeeCode);
        employee.setFirstName(request.firstName().trim());
        employee.setLastName(request.lastName().trim());
        employee.setEmail(email);
        employee.setPhone(normalizePhone(request.phone()));
        employee.setDesignation(request.designation().trim());
        employee.setSalary(request.salary());
        employee.setDateOfJoining(request.dateOfJoining());
        employee.setStatus(request.status());
        employee.setDepartment(department);
        employee.setProject(project);
    }

    private String normalizeCode(String value) {
        return value.trim().toUpperCase();
    }

    private String normalizeEmail(String value) {
        return value.trim().toLowerCase();
    }

    private String normalizePhone(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
