package com.aurodining.service.impl;

import com.aurodining.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Log-based Email Service (Development/Testing)
 * Prints verification code to logs instead of sending actual email
 * Use this for development/testing environments
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "email.provider", havingValue = "log", matchIfMissing = true)
public class LogEmailServiceImpl implements EmailService {

    @Value("${email.fixed-code:}")
    private String fixedCode;

    @Override
    public boolean sendVerificationCode(String email, String code) {
        // If fixed code is configured (for testing), use it
        if (fixedCode != null && !fixedCode.isEmpty()) {
            log.info("=== EMAIL VERIFICATION CODE (FIXED FOR TESTING) ===");
            log.info("Email: {}", email);
            log.info("Code: {}", fixedCode);
            log.info("================================================");
            return true;
        }

        // Otherwise, log the actual generated code
        log.info("=== EMAIL VERIFICATION CODE ===");
        log.info("Email: {}", email);
        log.info("Code: {}", code);
        log.info("Note: In production, this should be sent via AWS SES");
        log.info("=============================");
        return true;
    }
}
