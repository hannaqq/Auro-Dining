package com.aurodining.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class AppJwtUtilTests {
    @Test
    void newlyIssuedTokenIsAccepted() {
        Claims claims = AppJwtUtil.getClaimsBody(AppJwtUtil.getToken(42L));
        assertNotNull(claims);
        assertEquals(42L, ((Number) claims.get("id")).longValue());
        assertEquals(0, AppJwtUtil.verifyToken(claims));
    }

    @Test
    void tokenWithinLastFiveMinutesIsStillAccepted() {
        Claims claims = Jwts.claims().setExpiration(new Date(System.currentTimeMillis() + 60_000));
        assertEquals(0, AppJwtUtil.verifyToken(claims));
    }

    @Test
    void expiredClaimsAreRejected() {
        Claims claims = Jwts.claims().setExpiration(new Date(System.currentTimeMillis() - 60_000));
        assertEquals(1, AppJwtUtil.verifyToken(claims));
    }

    @Test
    void expirationAtCurrentTimeIsRejected() {
        Claims claims = Jwts.claims().setExpiration(new Date());
        assertEquals(1, AppJwtUtil.verifyToken(claims));
    }

    @Test
    void missingClaimsAreRejected() {
        assertEquals(1, AppJwtUtil.verifyToken(null));
    }

    @Test
    void missingExpirationIsRejected() {
        assertEquals(2, AppJwtUtil.verifyToken(Jwts.claims()));
    }
}
