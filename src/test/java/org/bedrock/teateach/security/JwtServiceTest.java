package org.bedrock.teateach.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;
    private final String SECRET_KEY = "4oieE5gg2JYdqGsjtdjSksosopO3mxnLKqABCznmTVU=";
    private final long EXPIRATION = 3600000; // 1 hour

    @BeforeEach
    public void setup() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION);
    }

    @Test
    public void testGenerateAndValidateToken() {
        // Given
        String username = "testuser";
        String userType = "STUDENT";
        String referenceId = "S12345";

        // When
        String token = jwtService.generateToken(username, userType, referenceId);

        // Then
        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
        assertEquals(username, jwtService.extractUsername(token));

        Claims claims = jwtService.extractAllClaims(token);
        assertEquals(userType, claims.get("userType"));
        assertEquals(referenceId, claims.get("referenceId"));
    }

    @Test
    public void testInvalidToken() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When & Then
        assertFalse(jwtService.validateToken(invalidToken));
    }

    @Test
    public void testExtractClaim() {
        // Given
        String username = "testuser";
        String userType = "TEACHER";
        String referenceId = "T12345";
        String token = jwtService.generateToken(username, userType, referenceId);

        // When & Then
        assertEquals(username, jwtService.extractClaim(token, Claims::getSubject));
        assertEquals(userType, jwtService.extractClaim(token, claims -> claims.get("userType", String.class)));
        assertEquals(referenceId, jwtService.extractClaim(token, claims -> claims.get("referenceId", String.class)));
    }
}
