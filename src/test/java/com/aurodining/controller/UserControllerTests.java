package com.aurodining.controller;

import com.aurodining.common.CustomException;
import com.aurodining.common.GlobalExceptionHandler;
import com.aurodining.dto.LoginResult;
import com.aurodining.entity.User;
import com.aurodining.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTests {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void sendMessageDelegatesToServiceAndMapsBusinessError() throws Exception {
        mockMvc.perform(post("/user/sendMsg")
                        .contentType("application/json")
                        .content("{\"email\":\"ada@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
        verify(userService).sendVerificationCode("ada@example.com");

        doThrow(new CustomException("Please provide email address"))
                .when(userService).sendVerificationCode(null);
        mockMvc.perform(post("/user/sendMsg")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("Please provide email address"));
    }

    @Test
    void loginDelegatesCredentialsAndSetsCookie() throws Exception {
        User user = new User();
        user.setId(8L);
        user.setEmail("ada@example.com");
        when(userService.loginByEmailCode("ada@example.com", "1234"))
                .thenReturn(new LoginResult<>(user, "jwt-token"));

        MvcResult result = mockMvc.perform(post("/user/login")
                        .contentType("application/json")
                        .content("{\"email\":\"ada@example.com\",\"code\":\"1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(8))
                .andReturn();

        Cookie cookie = result.getResponse().getCookie("Auth-Token");
        assertNotNull(cookie);
        assertEquals("jwt-token", cookie.getValue());
        assertEquals("/", cookie.getPath());
        assertEquals(86400, cookie.getMaxAge());
    }

    @Test
    void loginMapsServiceFailureWithoutSettingCookie() throws Exception {
        when(userService.loginByEmailCode("disabled@example.com", "1234"))
                .thenThrow(new CustomException("Login failed: Account is disabled"));

        MvcResult result = mockMvc.perform(post("/user/login")
                        .contentType("application/json")
                        .content("{\"email\":\"disabled@example.com\",\"code\":\"1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("Login failed: Account is disabled"))
                .andReturn();

        assertNull(result.getResponse().getCookie("Auth-Token"));
    }

    @Test
    void logoutClearsCookie() throws Exception {
        MvcResult logout = mockMvc.perform(post("/user/loginout"))
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();

        Cookie cookie = logout.getResponse().getCookie("Auth-Token");
        assertNotNull(cookie);
        assertEquals(0, cookie.getMaxAge());
    }
}
