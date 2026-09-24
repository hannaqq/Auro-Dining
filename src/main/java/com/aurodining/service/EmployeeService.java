package com.aurodining.service;

import com.aurodining.entity.Employee;
import org.springframework.data.domain.Page;

public interface EmployeeService {

    Employee getByUsername(String username);

    Employee login(String username, String password);

    Employee save(Employee employee);

    Employee getById(Long id);

    Page<Employee> page(int page, int pageSize, String name);
}