package com.example.employeeportal.repository;

import com.example.employeeportal.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("""
            SELECT e FROM Employee e
            JOIN FETCH e.department
            LEFT JOIN FETCH e.project
            WHERE :search = ''
               OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(e.designation) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(e.department.name) LIKE LOWER(CONCAT('%', :search, '%'))
            ORDER BY e.firstName ASC, e.lastName ASC
            """)
    List<Employee> search(@Param("search") String search);

    boolean existsByEmployeeCodeIgnoreCase(String employeeCode);

    boolean existsByEmployeeCodeIgnoreCaseAndIdNot(String employeeCode, Long id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
