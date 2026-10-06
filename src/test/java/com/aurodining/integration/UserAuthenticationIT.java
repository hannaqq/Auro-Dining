package com.aurodining.integration;

import com.aurodining.entity.User;
import com.aurodining.repository.UserRepository;
import com.aurodining.support.IntegrationTestContainers;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserAuthenticationIT extends IntegrationTestContainers {

    private static final String EMAIL = "integration@example.com";
    private static final String REDIS_KEY = "email:" + EMAIL;

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private StringRedisTemplate redisTemplate;

    @AfterEach
    void cleanData() {
        redisTemplate.delete(REDIS_KEY);
        User user = userRepository.findByEmail(EMAIL);
        if (user != null) {
            userRepository.delete(user);
        }
    }

    @Test
    void verificationCodeLoginCreatesUserCookieAndConsumesCode() throws Exception {
        mockMvc.perform(post("/user/sendMsg")
                        .servletPath("/user/sendMsg")
                        .contentType("application/json")
                        .content("{\"email\":\"" + EMAIL + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        assertEquals("1234", redisTemplate.opsForValue().get(REDIS_KEY));
        Long ttl = redisTemplate.getExpire(REDIS_KEY, TimeUnit.SECONDS);
        assertNotNull(ttl);
        assertTrue(ttl > 0 && ttl <= 300);

        MvcResult login = mockMvc.perform(post("/user/login")
                        .servletPath("/user/login")
                        .contentType("application/json")
                        .content("{\"email\":\"" + EMAIL + "\",\"code\":\"1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.email").value(EMAIL))
                .andReturn();

        Cookie authCookie = login.getResponse().getCookie("Auth-Token");
        assertNotNull(authCookie);
        assertNotNull(userRepository.findByEmail(EMAIL));
        assertNull(redisTemplate.opsForValue().get(REDIS_KEY));

        mockMvc.perform(get("/addressBook/list")
                        .servletPath("/addressBook/list")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isArray());
    }
}
