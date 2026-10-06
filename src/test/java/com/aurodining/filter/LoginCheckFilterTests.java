package com.aurodining.filter;

import com.aurodining.common.AppJwtUtil;
import com.aurodining.common.AuthContext;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class LoginCheckFilterTests {
    private final LoginCheckFilter filter = new LoginCheckFilter();

    @AfterEach
    void clearContext() {
        AuthContext.removeCurrentId();
    }

    @Test
    void publicEndpointDoesNotRequireToken() throws Exception {
        MockHttpServletRequest request = request("GET", "/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<Boolean> invoked = new AtomicReference<>(false);

        filter.doFilter(request, response, (req, res) -> invoked.set(true));

        assertTrue(invoked.get());
        assertNull(AuthContext.getCurrentId());
    }

    @Test
    void userCannotAccessAdminEndpoint() throws Exception {
        MockHttpServletRequest request = request("GET", "/admin/dish/page");
        request.setCookies(cookie("Auth-Token", 11L, AppJwtUtil.ROLE_USER));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> fail("request must not pass"));

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("FORBIDDEN"));
    }

    @Test
    void adminCannotAccessUserEndpoint() throws Exception {
        MockHttpServletRequest request = request("GET", "/addressBook/list");
        request.setCookies(cookie("Admin-Token", 22L, AppJwtUtil.ROLE_ADMIN));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> fail("request must not pass"));

        assertEquals(403, response.getStatus());
    }

    @Test
    void cookieOrderDoesNotChangeSelectedAdminIdentity() throws Exception {
        assertSelectedId("/admin/dish/page", 22L,
                cookie("Auth-Token", 11L, AppJwtUtil.ROLE_USER),
                cookie("Admin-Token", 22L, AppJwtUtil.ROLE_ADMIN));
        assertSelectedId("/admin/dish/page", 22L,
                cookie("Admin-Token", 22L, AppJwtUtil.ROLE_ADMIN),
                cookie("Auth-Token", 11L, AppJwtUtil.ROLE_USER));
    }

    @Test
    void sharedEndpointUsesUserIdentityWithBothCookies() throws Exception {
        assertSelectedId("/dish/list", 11L,
                cookie("Admin-Token", 22L, AppJwtUtil.ROLE_ADMIN),
                cookie("Auth-Token", 11L, AppJwtUtil.ROLE_USER));
    }

    @Test
    void wrongMethodIsNotTreatedAsSharedAccess() {
        assertEquals(LoginCheckFilter.AccessPolicy.USER_OR_ADMIN,
                filter.resolvePolicy("GET", "/dish/list"));
        assertEquals(LoginCheckFilter.AccessPolicy.DENY,
                filter.resolvePolicy("POST", "/dish/list"));
    }

    @Test
    void missingOrInvalidTokenReturnsExistingNotLoginPayload() throws Exception {
        MockHttpServletRequest request = request("GET", "/dish/list");
        request.setCookies(new Cookie("Auth-Token", "invalid"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> fail("request must not pass"));

        assertEquals(200, response.getStatus());
        assertTrue(response.getContentAsString().contains("NOTLOGIN"));
    }

    @Test
    void malformedClaimsAreRejected() throws Exception {
        assertRejected(signedToken(null, 11L, futureExpiration()));
        assertRejected(signedToken(AppJwtUtil.ROLE_USER, null, futureExpiration()));
        assertRejected(signedToken(AppJwtUtil.ROLE_USER, "11", futureExpiration()));
        assertRejected(signedToken(AppJwtUtil.ROLE_USER, 11L,
                new Date(System.currentTimeMillis() - 1_000)));
    }

    @Test
    void contextIsClearedAfterSuccessfulRequest() throws Exception {
        MockHttpServletRequest request = request("GET", "/addressBook/list");
        request.setCookies(cookie("Auth-Token", 11L, AppJwtUtil.ROLE_USER));

        filter.doFilter(request, new MockHttpServletResponse(),
                (req, res) -> assertEquals(11L, AuthContext.getCurrentId()));

        assertNull(AuthContext.getCurrentId());
    }

    @Test
    void unauthenticatedBackendPageRedirectsToBackendLogin() throws Exception {
        MockHttpServletResponse response = filterPageRequest("/backend/page/order/list.html");

        assertEquals(302, response.getStatus());
        assertEquals("/backend/page/login/login.html", response.getRedirectedUrl());
    }

    @Test
    void unauthenticatedFrontendPageRedirectsToFrontendLogin() throws Exception {
        MockHttpServletResponse response = filterPageRequest("/front/page/user.html");

        assertEquals(302, response.getStatus());
        assertEquals("/front/page/login.html", response.getRedirectedUrl());
    }

    @Test
    void unknownPathIsForbiddenByDefault() throws Exception {
        MockHttpServletRequest request = request("GET", "/unknown");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> fail("request must not pass"));

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("FORBIDDEN"));
    }

    private void assertSelectedId(String path, Long expectedId, Cookie... cookies) throws Exception {
        MockHttpServletRequest request = request("GET", path);
        request.setCookies(cookies);
        AtomicReference<Long> selectedId = new AtomicReference<>();
        filter.doFilter(request, new MockHttpServletResponse(),
                (req, res) -> selectedId.set(AuthContext.getCurrentId()));
        assertEquals(expectedId, selectedId.get());
        assertNull(AuthContext.getCurrentId());
    }

    private MockHttpServletResponse filterPageRequest(String path) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request("GET", path), response,
                (req, res) -> fail("request must not pass"));
        return response;
    }

    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        return request;
    }

    private Cookie cookie(String name, Long id, String role) {
        return new Cookie(name, AppJwtUtil.getToken(id, role));
    }

    private void assertRejected(String token) throws Exception {
        MockHttpServletRequest request = request("GET", "/addressBook/list");
        request.setCookies(new Cookie("Auth-Token", token));
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> fail("request must not pass"));
        assertTrue(response.getContentAsString().contains("NOTLOGIN"));
    }

    private String signedToken(String role, Object id, Date expiration) {
        var builder = Jwts.builder().setExpiration(expiration);
        if (role != null) builder.claim("role", role);
        if (id != null) builder.claim("id", id);
        return builder.signWith(SignatureAlgorithm.HS512, AppJwtUtil.generalKey()).compact();
    }

    private Date futureExpiration() {
        return new Date(System.currentTimeMillis() + 60_000);
    }
}
