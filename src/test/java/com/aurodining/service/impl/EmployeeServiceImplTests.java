package com.aurodining.service.impl;

import com.aurodining.common.AppJwtUtil;
import com.aurodining.common.CustomException;
import com.aurodining.dto.LoginResult;
import com.aurodining.entity.Employee;
import com.aurodining.repository.EmployeeRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTests {

    @Mock
    private EmployeeRepository employeeRepository;

    private EmployeeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EmployeeServiceImpl(employeeRepository);
    }

    @Test
    void loginRejectsUnknownEmployee() {
        when(employeeRepository.findByUsername("admin")).thenReturn(null);

        assertEquals("username doesn't exist",
                assertThrows(CustomException.class,
                        () -> service.login("admin", "secret")).getMessage());
    }

    @Test
    void loginRejectsMissingOrWrongPassword() {
        Employee employee = employee(3L, 1, md5("secret"));
        when(employeeRepository.findByUsername("admin")).thenReturn(employee);

        assertEquals("password is wrong",
                assertThrows(CustomException.class,
                        () -> service.login("admin", null)).getMessage());
        assertEquals("password is wrong",
                assertThrows(CustomException.class,
                        () -> service.login("admin", "wrong")).getMessage());
    }

    @Test
    void loginRejectsDisabledOrStatuslessEmployee() {
        Employee employee = employee(3L, 0, md5("secret"));
        when(employeeRepository.findByUsername("admin")).thenReturn(employee);

        assertEquals("the account is abandoned",
                assertThrows(CustomException.class,
                        () -> service.login("admin", "secret")).getMessage());

        employee.setStatus(null);
        assertEquals("the account is abandoned",
                assertThrows(CustomException.class,
                        () -> service.login("admin", "secret")).getMessage());
    }

    @Test
    void loginReturnsEmployeeAndAdminToken() {
        Employee employee = employee(3L, 1, md5("secret"));
        when(employeeRepository.findByUsername("admin")).thenReturn(employee);

        LoginResult<Employee> result = service.login("admin", "secret");

        assertSame(employee, result.principal());
        Claims claims = AppJwtUtil.getClaimsBody(result.token());
        assertEquals(3L, ((Number) claims.get("id")).longValue());
        assertEquals(AppJwtUtil.ROLE_ADMIN, claims.get("role", String.class));
    }

    private Employee employee(Long id, Integer status, String password) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setUsername("admin");
        employee.setStatus(status);
        employee.setPassword(password);
        return employee;
    }

    private String md5(String value) {
        return DigestUtils.md5DigestAsHex(value.getBytes(StandardCharsets.UTF_8));
    }
}
