package com.aurodining.controller.admin;

import com.aurodining.common.AppJwtUtil;
import com.aurodining.common.R;
import com.aurodining.entity.Employee;
import com.aurodining.service.EmployeeService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/employee")
@RequiredArgsConstructor
public class AdminEmployeeController {
    private final EmployeeService employeeService;

    @PostMapping("/login")
    public R<Employee> login(HttpServletResponse response, @RequestBody Employee employee) {
        String password = DigestUtils.md5DigestAsHex(employee.getPassword().getBytes());
        Employee existing = employeeService.getByUsername(employee.getUsername());
        if (existing == null) return R.error("username doesn't exist");
        if (!existing.getPassword().equals(password)) return R.error("password is wrong");
        if (existing.getStatus() == 0) return R.error("the account is abandoned");
        Cookie cookie = new Cookie("Admin-Token", AppJwtUtil.getToken(existing.getId(), AppJwtUtil.ROLE_ADMIN));
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        response.addCookie(cookie);
        return R.success(existing);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("Admin-Token", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return R.success("sign out success");
    }

    @PostMapping
    public R<String> save(@RequestBody Employee employee) {
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        employeeService.save(employee);
        return R.success("add success");
    }

    @GetMapping("/page")
    public R<Map<String, Object>> page(int page, int pageSize, String name) {
        Page<Employee> employees = employeeService.page(page, pageSize, name);
        Map<String, Object> result = new HashMap<>();
        result.put("records", employees.getContent());
        result.put("total", employees.getTotalElements());
        return R.success(result);
    }

    @PutMapping
    public R<String> update(@RequestBody Employee employee) {
        employeeService.save(employee);
        return R.success("update success");
    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        return employee == null ? R.error("no employee info") : R.success(employee);
    }
}
