package com.aurodining.controller.admin;

import com.aurodining.common.CustomException;
import com.aurodining.common.GlobalExceptionHandler;
import com.aurodining.dto.LoginResult;
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
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void loginDelegatesToServiceAndMapsBusinessError() throws Exception {
        when(employeeService.login("admin", "wrong"))
                .thenThrow(new CustomException("password is wrong"));

        performLogin("wrong")
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("password is wrong"));
    }

    @Test
    void successfulLoginAndLogoutManageAdminCookie() throws Exception {
        Employee employee = new Employee();
        employee.setId(3L);
        employee.setUsername("admin");
        when(employeeService.login("admin", "secret"))
                .thenReturn(new LoginResult<>(employee, "admin-jwt"));

        MvcResult login = performLogin("secret")
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();
        Cookie loginCookie = login.getResponse().getCookie("Admin-Token");
        assertNotNull(loginCookie);
        assertEquals("admin-jwt", loginCookie.getValue());
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

    private org.springframework.test.web.servlet.ResultActions performLogin(String password)
            throws Exception {
        return mockMvc.perform(post("/admin/employee/login")
                .contentType("application/json")
                .content("{\"username\":\"admin\",\"password\":\"" + password + "\"}"));
    }

    private String md5(String value) {
        return DigestUtils.md5DigestAsHex(value.getBytes());
    }
}
