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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import com.aurodining.common.AppJwtUtil;
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
    public R<Employee> login(HttpServletRequest request, HttpServletResponse response, @RequestBody Employee employee) {
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

        // Generate JWT and store in Cookie for stateless Admin Auth
        String token = AppJwtUtil.getToken(emp.getId());
        Cookie cookie = new Cookie("Admin-Token", token);
        cookie.setPath("/");
        cookie.setMaxAge(86400); // 24 hours
        response.addCookie(cookie);
        return R.success(emp);
    }


    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // Clear Admin JWT Cookie
        Cookie cookie = new Cookie("Admin-Token", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
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