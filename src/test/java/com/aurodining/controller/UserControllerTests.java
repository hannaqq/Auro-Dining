package com.aurodining.controller;

import com.aurodining.entity.User;
import com.aurodining.service.EmailService;
import com.aurodining.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.servlet.http.Cookie;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTests {

    @Mock private UserService userService;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private EmailService emailService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserController controller = new UserController(userService, redisTemplate, emailService);
        ReflectionTestUtils.setField(controller, "fixedCode", "1234");
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void sendMessageValidatesEmailAndStoresCodeForFiveMinutes() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(emailService.sendVerificationCode("ada@example.com", "1234")).thenReturn(true);

        mockMvc.perform(post("/user/sendMsg")
                        .contentType("application/json")
                        .content("{\"email\":\"ada@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(valueOperations).set(
                "email:ada@example.com", "1234", 5, TimeUnit.MINUTES);

        mockMvc.perform(post("/user/sendMsg")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void loginRejectsMissingOrIncorrectVerificationCode() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("email:ada@example.com")).thenReturn("1234");

        mockMvc.perform(post("/user/login")
                        .contentType("application/json")
                        .content("{\"code\":\"1234\"}"))
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/user/login")
                        .contentType("application/json")
                        .content("{\"email\":\"ada@example.com\",\"code\":\"9999\"}"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("Login failed: Invalid code"));
        verifyNoInteractions(userService);
    }

    @Test
    void loginExistingUserSetsCookieAndConsumesCode() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("email:ada@example.com")).thenReturn("1234");
        User existing = new User();
        existing.setId(8L);
        existing.setEmail("ada@example.com");
        when(userService.getByEmail("ada@example.com")).thenReturn(existing);

        MvcResult result = mockMvc.perform(post("/user/login")
                        .contentType("application/json")
                        .content("{\"email\":\"ada@example.com\",\"code\":\"1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(8))
                .andReturn();

        Cookie cookie = result.getResponse().getCookie("Auth-Token");
        assertNotNull(cookie);
        assertEquals("/", cookie.getPath());
        assertEquals(86400, cookie.getMaxAge());
        verify(redisTemplate).delete("email:ada@example.com");
        verify(userService, never()).save(any());
    }

    @Test
    void loginCreatesNewUserAndLogoutClearsCookie() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("email:new@example.com")).thenReturn("1234");
        when(userService.getByEmail("new@example.com")).thenReturn(null);
        when(userService.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(9L);
            return user;
        });

        mockMvc.perform(post("/user/login")
                        .contentType("application/json")
                        .content("{\"email\":\"new@example.com\",\"code\":\"1234\"}"))
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(9))
                .andExpect(jsonPath("$.data.status").value(1));

        MvcResult logout = mockMvc.perform(post("/user/loginout"))
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();
        Cookie cookie = logout.getResponse().getCookie("Auth-Token");
        assertNotNull(cookie);
        assertEquals(0, cookie.getMaxAge());
    }
}
