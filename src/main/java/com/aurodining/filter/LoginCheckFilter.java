package com.aurodining.filter;

import com.alibaba.fastjson.JSON;
import com.aurodining.common.AuthContext;
import com.aurodining.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import jakarta.servlet.http.Cookie;
import io.jsonwebtoken.Claims;
import com.aurodining.common.AppJwtUtil;

/**
 * Checks JWT authentication and manages request-scoped ThreadLocal context.
 * A Servlet Filter provides a shared authentication entry point before Spring MVC
 * dispatches requests, without depending on a specific MVC handler. It applies
 * the allowlist and handles unauthenticated page and API requests consistently.
 * Spring MVC interceptors can also intercept static resources served by Spring MVC;
 * serving static resources does not itself require a Filter.
 */
@WebFilter(urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestPath = request.getServletPath();
        log.info("Intercepted request: {}", requestPath);

        // Define white list
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/page/login/login.html",
                "/backend/js/**",
                "/backend/api/**",
                "/backend/styles/**",
                "/backend/images/**",
                "/backend/plugins/**",
                "/backend/favicon.ico",
                "/front/page/login.html",
                "/front/index.html",
                "/front/js/**",
                "/front/styles/**",
                "/front/images/**",
                "/front/api/**",
                "/front/plugins/**",
                "/user/sendMsg",
                "/user/login",
                "/common/download"
        };

        // Check if the path needs to be handled
        boolean check = check(urls, requestPath);
        if (check) {
            log.info("Path {} is in white list, passing...", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        // ThreadLocal logic with proper cleanup
        try {
            boolean adminRequest = isAdminRequest(request);
            boolean authenticatedButForbidden = false;

            // Check all recognized JWT cookies. The signed role claim, rather than
            // the cookie name, determines whether the caller is a user or admin.
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (!"Admin-Token".equals(cookie.getName())
                            && !"Auth-Token".equals(cookie.getName())) {
                        continue;
                    }

                    try {
                        Claims claims = AppJwtUtil.getClaimsBody(cookie.getValue());
                        if (claims == null || AppJwtUtil.verifyToken(claims) != 0) {
                            continue;
                        }

                        String role = claims.get("role", String.class);
                        if (!AppJwtUtil.ROLE_USER.equals(role)
                                && !AppJwtUtil.ROLE_ADMIN.equals(role)) {
                            continue;
                        }

                        if (adminRequest && !AppJwtUtil.ROLE_ADMIN.equals(role)) {
                            authenticatedButForbidden = true;
                            continue;
                        }

                        Long id = ((Number) claims.get("id")).longValue();
                        AuthContext.setCurrentId(id);
                        log.info("Authenticated request via JWT, role={}, id={}", role, id);
                        filterChain.doFilter(request, response);
                        return;
                    } catch (Exception e) {
                        log.warn("Invalid or expired JWT token", e);
                    }
                }
            }

            if (authenticatedButForbidden) {
                log.warn("Forbidden request to admin resource: {}", requestPath);
                handleForbidden(request, response, requestPath);
                return;
            }

            // Handle unauthorized access
            log.info("Unauthorized access to: {}", requestPath);
            handleUnauthorized(request, response, requestPath);

        } finally {
            // Cleanup threadLocal to prevent data contamination and memory leaks
            AuthContext.removeCurrentId();
            log.debug("ThreadLocal context cleared for URI: {}", requestPath);
        }
    }

    private boolean isAdminRequest(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getServletPath();

        if (uri.startsWith("/backend/")) {
            return true;
        }

        if (uri.equals("/employee") || uri.startsWith("/employee/")) {
            return true;
        }

        if (uri.startsWith("/category")) {
            return !("GET".equals(method) && "/category/list".equals(uri));
        }

        if (uri.startsWith("/dish")) {
            return !("GET".equals(method) && "/dish/list".equals(uri));
        }

        if (uri.startsWith("/combo")) {
            boolean userEndpoint = "GET".equals(method)
                    && ("/combo/list".equals(uri)
                    || PATH_MATCHER.match("/combo/dish/{id}", uri));
            return !userEndpoint;
        }

        if ("GET".equals(method) && "/order/page".equals(uri)) {
            return true;
        }

        if ("PUT".equals(method) && "/order".equals(uri)) {
            return true;
        }

        return "POST".equals(method) && "/common/upload".equals(uri);
    }

    private void handleForbidden(HttpServletRequest request, HttpServletResponse response, String uri) throws IOException {
        String xRequestedWith = request.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest".equals(xRequestedWith);

        if (uri.endsWith(".html") && !isAjax) {
            response.sendRedirect("/backend/page/login/login.html");
            return;
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(JSON.toJSONString(R.error("FORBIDDEN")));
    }

    private void handleUnauthorized(HttpServletRequest request, HttpServletResponse response, String uri) throws IOException {
        String xRequestedWith = request.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest".equals(xRequestedWith);

        if (uri.endsWith(".html") && !isAjax) {
            // Determine redirect URL based on request path
            if (uri.startsWith("/backend")) {
                // Backend management pages redirect to backend login page
                response.sendRedirect("/backend/page/login/login.html");
            } else {
                // Frontend pages redirect to frontend login page
                response.sendRedirect("/front/page/login.html");
            }
        } else {
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        }
    }

    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            if (PATH_MATCHER.match(url, requestURI)) return true;
        }
        return false;
    }
}
