package com.aurodining.filter;

import com.alibaba.fastjson.JSON;
import com.aurodining.common.AppJwtUtil;
import com.aurodining.common.AuthContext;
import com.aurodining.common.R;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;

@WebFilter(urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    public enum AccessPolicy { PUBLIC, USER_ONLY, ADMIN_ONLY, USER_OR_ADMIN, DENY }

    private record Principal(Long id) {}

    private static final String[] PUBLIC_GET_PATHS = {
            "/common/download", "/health",
            "/backend/page/login/login.html", "/front/page/login.html", "/front/index.html",
            "/backend/js/**", "/backend/api/**", "/backend/styles/**", "/backend/images/**",
            "/backend/fonts/**", "/backend/plugins/**", "/backend/favicon.ico",
            "/front/js/**", "/front/api/**", "/front/styles/**", "/front/images/**",
            "/front/fonts/**", "/front/plugins/**"
    };

    private static final String[] PUBLIC_POST_PATHS = {
            "/user/sendMsg", "/user/login", "/user/loginout",
            "/admin/employee/login", "/admin/employee/logout"
    };

    private static final String[] SHARED_GET_PATHS = {
            "/dish/list", "/combo/list", "/combo/dish/{id}", "/category/list"
    };

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String path = request.getServletPath();
        AccessPolicy policy = resolvePolicy(request.getMethod(), path);
        try {
            if (policy == AccessPolicy.PUBLIC) {
                chain.doFilter(request, response);
                return;
            }
            if (policy == AccessPolicy.DENY) {
                handleForbidden(request, response, path);
                return;
            }

            Principal user = readPrincipal(request, "Auth-Token", AppJwtUtil.ROLE_USER);
            Principal admin = readPrincipal(request, "Admin-Token", AppJwtUtil.ROLE_ADMIN);
            Principal selected = selectPrincipal(policy, user, admin);
            if (selected != null) {
                AuthContext.setCurrentId(selected.id());
                chain.doFilter(request, response);
                return;
            }
            if (user != null || admin != null) handleForbidden(request, response, path);
            else handleUnauthorized(request, response, path);
        } finally {
            AuthContext.removeCurrentId();
        }
    }

    public AccessPolicy resolvePolicy(String method, String path) {
        if (isPublicRequest(method, path)) return AccessPolicy.PUBLIC;
        if (isSharedRequest(method, path)) return AccessPolicy.USER_OR_ADMIN;
        if (isUserRequest(method, path)) return AccessPolicy.USER_ONLY;
        if (isAdminRequest(method, path)) return AccessPolicy.ADMIN_ONLY;
        return AccessPolicy.DENY;
    }

    private boolean isPublicRequest(String method, String path) {
        return ("GET".equals(method) && matchesAny(PUBLIC_GET_PATHS, path))
                || ("POST".equals(method) && matchesAny(PUBLIC_POST_PATHS, path));
    }

    private boolean isSharedRequest(String method, String path) {
        return "GET".equals(method) && matchesAny(SHARED_GET_PATHS, path);
    }

    private boolean isUserRequest(String method, String path) {
        return PATH_MATCHER.match("/addressBook/**", path)
                || PATH_MATCHER.match("/shoppingCart/**", path)
                || ("GET".equals(method) && "/order/userPage".equals(path))
                || ("POST".equals(method) && ("/order/submit".equals(path) || "/order/again".equals(path)))
                || ("GET".equals(method) && PATH_MATCHER.match("/front/page/**", path));
    }

    private boolean isAdminRequest(String method, String path) {
        return PATH_MATCHER.match("/admin/**", path)
                || ("GET".equals(method) && PATH_MATCHER.match("/backend/**", path));
    }

    private boolean matchesAny(String[] patterns, String path) {
        for (String pattern : patterns) {
            if (PATH_MATCHER.match(pattern, path)) return true;
        }
        return false;
    }

    private Principal selectPrincipal(AccessPolicy policy, Principal user, Principal admin) {
        return switch (policy) {
            case USER_ONLY -> user;
            case ADMIN_ONLY -> admin;
            case USER_OR_ADMIN -> user != null ? user : admin;
            default -> null;
        };
    }

    private Principal readPrincipal(HttpServletRequest request, String cookieName, String requiredRole) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (!cookieName.equals(cookie.getName())) continue;
            try {
                Claims claims = AppJwtUtil.getClaimsBody(cookie.getValue());
                if (AppJwtUtil.verifyToken(claims) != 0 || !requiredRole.equals(claims.get("role", String.class))) return null;
                Object id = claims.get("id");
                if (!(id instanceof Number number)) return null;
                return new Principal(number.longValue());
            } catch (Exception exception) {
                log.warn("Rejected invalid {} cookie", cookieName);
                return null;
            }
        }
        return null;
    }

    private void handleForbidden(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
        if (isHtmlNavigation(request, path)) {
            response.sendRedirect(path.startsWith("/backend") ? "/backend/page/login/login.html" : "/front/page/login.html");
            return;
        }
        writeError(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN");
    }

    private void handleUnauthorized(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
        if (isHtmlNavigation(request, path)) {
            response.sendRedirect(path.startsWith("/backend") ? "/backend/page/login/login.html" : "/front/page/login.html");
            return;
        }
        writeError(response, HttpServletResponse.SC_OK, "NOTLOGIN");
    }

    private boolean isHtmlNavigation(HttpServletRequest request, String path) {
        return path.endsWith(".html") && !"XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(JSON.toJSONString(R.error(message)));
    }
}
