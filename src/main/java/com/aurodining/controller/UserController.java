package com.aurodining.controller;

import com.aurodining.common.AuthContext;
import com.aurodining.common.R;
import com.aurodining.entity.User;
import com.aurodining.service.EmailService;
import com.aurodining.service.UserService;
import org.apache.commons.lang.RandomStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.dao.DataIntegrityViolationException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Controller for User Frontend - User Authentication
 * Handles user login, logout, and verification code sending for mobile clients
 */
@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /** Use StringRedisTemplate so verification code is stored/read as plain string (avoids Jackson serialization mismatch). */
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired(required = false)
    private EmailService emailService;

    // For local testing, use fixedCode and store it in Redis
    @Value("${email.fixed-code:}")
    private String fixedCode;

    /**
     * Send verification code via email
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user){
        String email = user.getEmail();
        
        if(email == null || email.isEmpty()){
            return R.error("Please provide email address");
        }
        
        try {
            // Use fixed code for local testing when configured; otherwise generate random code
            String code;
            if (fixedCode != null && !fixedCode.trim().isEmpty()) {
                code = fixedCode.trim();
                log.info("Using fixed verification code for email {}: {}", email, code);
            } else {
                code = RandomStringUtils.randomNumeric(4);
                log.info("Verification code for email {}: {}", email, code);
            }

            // Send email via configured service (AWS SES or Log)
            if (emailService != null) {
                boolean sent = emailService.sendVerificationCode(email, code);
                if (!sent) {
                    log.warn("Failed to send email, but code is still stored in Redis for testing");
                }
            } else {
                log.info("No email service configured. Code: {}", code);
            }

            // Store code in Redis for 5 minutes (plain string, use email as key)
            stringRedisTemplate.opsForValue().set("email:" + email, code, 5, TimeUnit.MINUTES);

            return R.success("Verification code sent to email");
        } catch (Exception e) {
            log.error("Failed to send verification code to email", e);
            return R.error("Service temporarily unavailable. Please try again later.");
        }
    }

    /**
     * Mobile User Login via email
     */
    @PostMapping("/login")
    public R<User> login(HttpServletRequest request, @RequestBody Map map){
        Object emailObj = map.get("email");
        Object codeObj = map.get("code");

        if(emailObj == null || emailObj.toString().isEmpty()){
            return R.error("Login failed: Email is missing");
        }
        
        if(codeObj == null){
            return R.error("Login failed: Verification code is missing");
        }

        String email = emailObj.toString().trim();
        String code = codeObj != null ? codeObj.toString().trim() : "";
        log.info("Attempting login: email={}, code={}", email, code);

        try {
            // 1. Verify code from Redis (plain string)
            String cacheKey = "email:" + email;
            String cachedCode = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cachedCode == null || !cachedCode.equals(code)) {
                log.warn("Invalid code: cachedCode={}, inputCode={}", cachedCode, code);
                return R.error("Login failed: Invalid code");
            }

            // 2. Check if user exists in database
            User user = userService.getByEmail(email);

            // 3. Auto-register if user is new
            if(user == null){
                user = new User();
                user.setEmail(email);
                user.setStatus(1); // Default status: enabled
                userService.save(user);
            }

            // 4. Store user ID in session
            request.getSession().setAttribute("user", user.getId());

            // 5. Clear Redis code after successful login
            stringRedisTemplate.delete(cacheKey);

            return R.success(user);
        } catch (DataIntegrityViolationException e) {
            log.error("Login failed: user table sequence out of sync (duplicate key). Run: SELECT setval(pg_get_serial_sequence('\"user\"', 'id'), (SELECT COALESCE(MAX(id), 1) FROM \"user\"));", e);
            return R.error("Login failed: server database error. Please try again or contact support.");
        } catch (Exception e) {
            log.error("Login failed", e);
            return R.error("Service temporarily unavailable. Please try again later.");
        }
    }

    /**
     * Mobile User Logout
     */
    @PostMapping("/loginout")
    public R<String> loginout(HttpServletRequest request) {
        // 1. Remove user ID from session
        request.getSession().removeAttribute("user");
        // 2. Clear current thread local user context
        AuthContext.removeCurrentId();
        log.info("User logged out successfully");
        return R.success("Logout successful");
    }
}