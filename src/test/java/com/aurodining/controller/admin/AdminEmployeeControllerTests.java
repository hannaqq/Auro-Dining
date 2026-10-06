package com.aurodining.controller.admin;

import com.aurodining.entity.Employee;
import com.aurodining.service.EmployeeService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.DigestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
class AdminEmployeeControllerTests {

    @Mock
    private EmployeeService employeeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminEmployeeController(employeeService))
                .build();
    }

    @Test
    void loginRejectsUnknownWrongPasswordAndDisabledAccounts() throws Exception {
        when(employeeService.getByUsername("admin")).thenReturn(null);
        performLogin().andExpect(jsonPath("$.msg").value("username doesn't exist"));

        Employee existing = employee("different", 1);
        when(employeeService.getByUsername("admin")).thenReturn(existing);
        performLogin().andExpect(jsonPath("$.msg").value("password is wrong"));

        existing.setPassword(md5("secret"));
        existing.setStatus(0);
        performLogin().andExpect(jsonPath("$.msg").value("the account is abandoned"));
    }

    @Test
    void successfulLoginAndLogoutManageAdminCookie() throws Exception {
        Employee existing = employee(md5("secret"), 1);
        existing.setId(3L);
        when(employeeService.getByUsername("admin")).thenReturn(existing);

        MvcResult login = performLogin()
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();
        Cookie loginCookie = login.getResponse().getCookie("Admin-Token");
        assertNotNull(loginCookie);
        assertEquals("/", loginCookie.getPath());
        assertEquals(86400, loginCookie.getMaxAge());

        MvcResult logout = mockMvc.perform(post("/admin/employee/logout"))
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();
        Cookie logoutCookie = logout.getResponse().getCookie("Admin-Token");
        assertNotNull(logoutCookie);
        assertEquals(0, logoutCookie.getMaxAge());
    }

    @Test
    void saveAlwaysAssignsDefaultHashedPassword() throws Exception {
        mockMvc.perform(post("/admin/employee")
                        .contentType("application/json")
                        .content("{\"username\":\"new-admin\",\"password\":\"ignored\"}"))
                .andExpect(jsonPath("$.code").value(1));

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeService).save(captor.capture());
        assertEquals(md5("123456"), captor.getValue().getPassword());
    }

    private org.springframework.test.web.servlet.ResultActions performLogin() throws Exception {
        return mockMvc.perform(post("/admin/employee/login")
                .contentType("application/json")
                .content("{\"username\":\"admin\",\"password\":\"secret\"}"));
    }

    private Employee employee(String password, int status) {
        Employee employee = new Employee();
        employee.setUsername("admin");
        employee.setPassword(password);
        employee.setStatus(status);
        return employee;
    }

    private String md5(String value) {
        return DigestUtils.md5DigestAsHex(value.getBytes());
    }
}
