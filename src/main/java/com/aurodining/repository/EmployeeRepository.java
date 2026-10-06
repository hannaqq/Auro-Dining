package com.aurodining.repository;

import com.aurodining.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Employee findByUsername(String username);

    Page<Employee> findByNameContaining(String name, Pageable pageable);
}
