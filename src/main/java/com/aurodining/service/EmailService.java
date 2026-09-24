package com.aurodining.service;

/**
 * Email Service Interface
 * Provides abstraction for sending verification codes via Email
 */
public interface EmailService {
    /**
     * Send verification code to email address
     * @param email Email address
     * @param code Verification code
     * @return true if sent successfully, false otherwise
     */
    boolean sendVerificationCode(String email, String code);
}
