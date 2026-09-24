package com.aurodining.service.impl;

import com.aurodining.entity.Employee;
import com.aurodining.repository.EmployeeRepository;
import com.aurodining.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Employee getByUsername(String username) {
        return employeeRepository.findByUsername(username);
    }

    @Override
    public Employee login(String username, String password) {
        return this.getByUsername(username);
    }

    @Override
    @Transactional
    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }

    @Override
    public Employee getById(Long id) {
        return employeeRepository.findById(id).orElse(null);
    }

    @Override
    public Page<Employee> page(int page, int pageSize, String name) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("updateTime").descending());
        if (name != null && !"".equals(name)) {
            return employeeRepository.findByNameContaining(name, pageable);
        } else {
            return employeeRepository.findAll(pageable);
        }
    }
}
