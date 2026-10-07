package com.aurodining.controller;

import lombok.RequiredArgsConstructor;

import com.aurodining.common.AuthContext;
import com.aurodining.common.R;
import com.aurodining.dto.LoginResult;
import com.aurodining.entity.User;
import com.aurodining.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Controller for User Frontend - User Authentication
 * Handles user login, logout, and verification code sending for mobile clients
 */
@RestController
@Slf4j
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Send verification code via email
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user){
        userService.sendVerificationCode(user == null ? null : user.getEmail());
        return R.success("Verification code sent to email");
    }

    /**
     * Mobile User Login via email
     */
    @PostMapping("/login")
    public R<User> login(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> map){
        Object emailObj = map.get("email");
        Object codeObj = map.get("code");

        String email = emailObj == null ? null : emailObj.toString();
        String code = codeObj == null ? null : codeObj.toString();
        LoginResult<User> result = userService.loginByEmailCode(email, code);

        Cookie cookie = new Cookie("Auth-Token", result.token());
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        response.addCookie(cookie);
        return R.success(result.principal());
    }

    /**
     * Mobile User Logout
     */
    @PostMapping("/loginout")
    public R<String> loginout(HttpServletRequest request, HttpServletResponse response) {
        // 1. Clear JWT Cookie
        Cookie cookie = new Cookie("Auth-Token", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        
        // 2. Clear current thread local user context
        AuthContext.removeCurrentId();
        log.info("User logged out successfully");
        return R.success("Logout successful");
    }
}
