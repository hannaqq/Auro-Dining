package com.aurodining.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.aurodining.common.AppJwtUtil;
import com.aurodining.common.CustomException;
import com.aurodining.dto.LoginResult;
import com.aurodining.entity.User;
import com.aurodining.repository.UserRepository;
import com.aurodining.service.EmailService;
import com.aurodining.service.UserService;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final EmailService emailService;

    @Value("${email.fixed-code:}")
    private String fixedCode;

    @Override
    public void sendVerificationCode(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new CustomException("Please provide email address");
        }

        String normalizedEmail = email.trim();
        try {
            String code = fixedCode == null || fixedCode.trim().isEmpty()
                    ? RandomStringUtils.randomNumeric(4)
                    : fixedCode.trim();

            boolean sent = emailService.sendVerificationCode(normalizedEmail, code);
            if (!sent) {
                log.warn("Failed to send email, but code is still stored in Redis for testing");
            }

            stringRedisTemplate.opsForValue().set(
                    verificationCodeKey(normalizedEmail), code, 5, TimeUnit.MINUTES);
        } catch (Exception exception) {
            log.error("Failed to send verification code to email", exception);
            throw new CustomException("Service temporarily unavailable. Please try again later.");
        }
    }

    @Override
    public LoginResult<User> loginByEmailCode(String email, String code) {
        if (email == null || email.trim().isEmpty()) {
            throw new CustomException("Login failed: Email is missing");
        }
        if (code == null) {
            throw new CustomException("Login failed: Verification code is missing");
        }

        String normalizedEmail = email.trim();
        String normalizedCode = code.trim();
        String cacheKey = verificationCodeKey(normalizedEmail);

        try {
            String cachedCode = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cachedCode == null || !cachedCode.equals(normalizedCode)) {
                throw new CustomException("Login failed: Invalid code");
            }

            User user = userRepository.findByEmail(normalizedEmail);
            if (user == null) {
                user = new User();
                user.setEmail(normalizedEmail);
                user.setStatus(1);
                user = userRepository.save(user);
            }

            if (Integer.valueOf(0).equals(user.getStatus())) {
                throw new CustomException("Login failed: Account is disabled");
            }

            String token = AppJwtUtil.getToken(user.getId(), AppJwtUtil.ROLE_USER);
            stringRedisTemplate.delete(cacheKey);
            return new LoginResult<>(user, token);
        } catch (CustomException exception) {
            throw exception;
        } catch (DataIntegrityViolationException exception) {
            log.error("Login failed: user table sequence is out of sync", exception);
            throw new CustomException(
                    "Login failed: server database error. Please try again or contact support.");
        } catch (Exception exception) {
            log.error("Login failed", exception);
            throw new CustomException("Service temporarily unavailable. Please try again later.");
        }
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    private String verificationCodeKey(String email) {
        return "email:" + email;
    }
}