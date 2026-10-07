package com.aurodining.service.impl;

import com.aurodining.common.AppJwtUtil;
import com.aurodining.common.CustomException;
import com.aurodining.dto.LoginResult;
import com.aurodining.entity.User;
import com.aurodining.repository.UserRepository;
import com.aurodining.service.EmailService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTests {

    @Mock private UserRepository userRepository;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private EmailService emailService;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(userRepository, redisTemplate, emailService);
        ReflectionTestUtils.setField(service, "fixedCode", "1234");
    }

    @Test
    void sendVerificationCodeValidatesEmailAndStoresFixedCode() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(emailService.sendVerificationCode("ada@example.com", "1234")).thenReturn(false);

        service.sendVerificationCode(" ada@example.com ");

        verify(valueOperations).set(
                "email:ada@example.com", "1234", 5, TimeUnit.MINUTES);
        assertEquals("Please provide email address",
                assertThrows(CustomException.class,
                        () -> service.sendVerificationCode(" ")).getMessage());
    }

    @Test
    void sendVerificationCodeGeneratesFourDigitCodeWhenFixedCodeIsEmpty() {
        ReflectionTestUtils.setField(service, "fixedCode", "");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(emailService.sendVerificationCode(eq("ada@example.com"), anyString()))
                .thenReturn(true);

        service.sendVerificationCode("ada@example.com");

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(
                eq("email:ada@example.com"), codeCaptor.capture(), eq(5L),
                eq(TimeUnit.MINUTES));
        assertTrue(codeCaptor.getValue().matches("\\d{4}"));
    }

    @Test
    void loginRejectsMissingOrInvalidCredentialsWithoutConsumingCode() {
        assertEquals("Login failed: Email is missing",
                assertThrows(CustomException.class,
                        () -> service.loginByEmailCode(null, "1234")).getMessage());
        assertEquals("Login failed: Verification code is missing",
                assertThrows(CustomException.class,
                        () -> service.loginByEmailCode("ada@example.com", null)).getMessage());

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("email:ada@example.com")).thenReturn("1234");
        assertEquals("Login failed: Invalid code",
                assertThrows(CustomException.class,
                        () -> service.loginByEmailCode("ada@example.com", "9999")).getMessage());

        verify(redisTemplate, never()).delete(anyString());
        verifyNoInteractions(userRepository);
    }

    @Test
    void loginExistingUserReturnsUserTokenAndConsumesCode() {
        User user = user(8L, "ada@example.com", 1);
        stubValidCode("ada@example.com");
        when(userRepository.findByEmail("ada@example.com")).thenReturn(user);

        LoginResult<User> result =
                service.loginByEmailCode(" ada@example.com ", " 1234 ");

        assertSame(user, result.principal());
        Claims claims = AppJwtUtil.getClaimsBody(result.token());
        assertEquals(8L, ((Number) claims.get("id")).longValue());
        assertEquals(AppJwtUtil.ROLE_USER, claims.get("role", String.class));
        verify(redisTemplate).delete("email:ada@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginCreatesNewEnabledUser() {
        stubValidCode("new@example.com");
        when(userRepository.findByEmail("new@example.com")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(9L);
            return saved;
        });

        LoginResult<User> result =
                service.loginByEmailCode("new@example.com", "1234");

        assertEquals(9L, result.principal().getId());
        assertEquals(1, result.principal().getStatus());
        verify(redisTemplate).delete("email:new@example.com");
    }

    @Test
    void loginRejectsDisabledUserWithoutConsumingCodeOrRegistering() {
        User disabled = user(10L, "disabled@example.com", 0);
        stubValidCode("disabled@example.com");
        when(userRepository.findByEmail("disabled@example.com")).thenReturn(disabled);

        CustomException exception = assertThrows(CustomException.class,
                () -> service.loginByEmailCode("disabled@example.com", "1234"));

        assertEquals("Login failed: Account is disabled", exception.getMessage());
        verify(redisTemplate, never()).delete(anyString());
        verify(userRepository, never()).save(any());
    }

    private void stubValidCode(String email) {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("email:" + email)).thenReturn("1234");
    }

    private User user(Long id, String email, Integer status) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setStatus(status);
        return user;
    }
}
