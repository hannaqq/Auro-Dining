package com.aurodining.controller;

import com.aurodining.common.R;
import com.aurodining.entity.Employee;
import com.aurodining.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for Backend Management - Employee Management
 * Handles employee login, CRUD operations for backend administrators
 */
@RestController
@Slf4j
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        // Encrypt password using MD5
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        Employee emp = employeeService.getByUsername(employee.getUsername());

        if (emp == null) {
            return R.error("username doesn't exist");
        }

        if (!emp.getPassword().equals(password)) {
            return R.error("password is wrong");
        }

        if (emp.getStatus() == 0) {
            return R.error("the account is abandoned");
        }

        // Set session attribute
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }


    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employee");
        return R.success("sign out success");
    }

    /**
     * Add New Employee
     */
    @PostMapping
        public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("add employee: {}", employee.toString());

        // Default password
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        employeeService.save(employee);
        return R.success("add success");
    }

    /**
     * Pagination Query
     */
    @GetMapping("/page")
    public R<Map<String, Object>> selectPage(int page, int pageSize, String name) {
        Page<Employee> pageInfo = employeeService.page(page, pageSize, name);

        Map<String, Object> pageData = new HashMap<>();
        pageData.put("records", pageInfo.getContent());
        pageData.put("total", pageInfo.getTotalElements());

        return R.success(pageData);
    }

    /**
     * Update Employee Info
     */
    @PutMapping
        public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("update employee: {}", employee.toString());

        employeeService.save(employee);
        return R.success("update success");
    }

    /**
     * Get Employee by ID
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        log.info("get employee info by id...");
        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return R.success(employee);
        }
        return R.error("no employee info");
    }
}