package com.example.employeeportal.config;

import com.example.employeeportal.entity.Department;
import com.example.employeeportal.entity.Employee;
import com.example.employeeportal.entity.EmploymentStatus;
import com.example.employeeportal.entity.Project;
import com.example.employeeportal.repository.DepartmentRepository;
import com.example.employeeportal.repository.EmployeeRepository;
import com.example.employeeportal.repository.ProjectRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;

    public DataSeeder(DepartmentRepository departmentRepository,
                      ProjectRepository projectRepository,
                      EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.projectRepository = projectRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (departmentRepository.count() == 0) {
            departmentRepository.saveAll(List.of(
                    new Department("Engineering", "Bengaluru"),
                    new Department("Human Resources", "Pune"),
                    new Department("Finance", "Mumbai"),
                    new Department("Sales", "Delhi")
            ));
        }

        if (projectRepository.count() == 0) {
            projectRepository.saveAll(List.of(
                    new Project("Apollo", "Internal Platform", "ACTIVE"),
                    new Project("Nova", "Acme Corp", "ACTIVE"),
                    new Project("Atlas", "Globex", "COMPLETED")
            ));
        }

        if (employeeRepository.count() > 0) {
            return;
        }

        Map<String, Department> departments = departmentRepository.findAll().stream()
                .collect(Collectors.toMap(Department::getName, Function.identity()));
        Map<String, Project> projects = projectRepository.findAll().stream()
                .collect(Collectors.toMap(Project::getName, Function.identity()));

        employeeRepository.saveAll(List.of(
                employee("EMP001", "Aarav", "Sharma", "aarav.sharma@example.com", "+91 98765 43210",
                        "Senior Software Engineer", "92000", "2021-04-12", EmploymentStatus.ACTIVE,
                        departments.get("Engineering"), projects.get("Nova")),
                employee("EMP002", "Diya", "Patel", "diya.patel@example.com", "+91 98220 11445",
                        "HR Executive", "58000", "2022-07-18", EmploymentStatus.ACTIVE,
                        departments.get("Human Resources"), projects.get("Apollo")),
                employee("EMP003", "Rohan", "Iyer", "rohan.iyer@example.com", "+91 99870 55221",
                        "Financial Analyst", "67000", "2020-01-06", EmploymentStatus.ON_LEAVE,
                        departments.get("Finance"), projects.get("Atlas")),
                employee("EMP004", "Ananya", "Rao", "ananya.rao@example.com", "+91 90080 77332",
                        "QA Engineer", "71000", "2023-02-20", EmploymentStatus.ACTIVE,
                        departments.get("Engineering"), projects.get("Nova")),
                employee("EMP005", "Kabir", "Mehta", "kabir.mehta@example.com", "+91 98111 66554",
                        "Sales Manager", "81000", "2019-11-04", EmploymentStatus.ACTIVE,
                        departments.get("Sales"), projects.get("Apollo")),
                employee("EMP006", "Meera", "Nair", "meera.nair@example.com", "+91 97654 88990",
                        "UI/UX Designer", "64000", "2022-09-12", EmploymentStatus.INACTIVE,
                        departments.get("Engineering"), projects.get("Atlas"))
        ));
    }

    private Employee employee(String code, String firstName, String lastName, String email, String phone,
                              String designation, String salary, String joiningDate, EmploymentStatus status,
                              Department department, Project project) {
        Employee employee = new Employee();
        employee.setEmployeeCode(code);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(email);
        employee.setPhone(phone);
        employee.setDesignation(designation);
        employee.setSalary(new BigDecimal(salary));
        employee.setDateOfJoining(LocalDate.parse(joiningDate));
        employee.setStatus(status);
        employee.setDepartment(department);
        employee.setProject(project);
        return employee;
    }
}
