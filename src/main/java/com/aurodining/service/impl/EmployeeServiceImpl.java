package com.aurodining.service.impl;

import lombok.RequiredArgsConstructor;

import com.aurodining.common.AppJwtUtil;
import com.aurodining.common.CustomException;
import com.aurodining.dto.LoginResult;
import com.aurodining.entity.Employee;
import com.aurodining.repository.EmployeeRepository;
import com.aurodining.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Override
    public Employee getByUsername(String username) {
        return employeeRepository.findByUsername(username);
    }

    @Override
    public LoginResult<Employee> login(String username, String password) {
        Employee employee = this.getByUsername(username);
        if (employee == null) {
            throw new CustomException("username doesn't exist");
        }
        if (password == null) {
            throw new CustomException("password is wrong");
        }

        String encodedPassword = DigestUtils.md5DigestAsHex(
                password.getBytes(StandardCharsets.UTF_8));
        if (!encodedPassword.equals(employee.getPassword())) {
            throw new CustomException("password is wrong");
        }
        if (!Integer.valueOf(1).equals(employee.getStatus())) {
            throw new CustomException("the account is abandoned");
        }

        String token = AppJwtUtil.getToken(employee.getId(), AppJwtUtil.ROLE_ADMIN);
        return new LoginResult<>(employee, token);
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
