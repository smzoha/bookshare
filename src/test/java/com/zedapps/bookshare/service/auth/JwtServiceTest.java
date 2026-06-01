package com.zedapps.bookshare.service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author smzoha
 * @since 2/5/26
 **/
@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    private static final String SECRET = "a-very-long-test-secret-key-for-jwt";
    private static final String ENCODED_SECRET = Base64.getEncoder().encodeToString(SECRET.getBytes(StandardCharsets.UTF_8));

    @InjectMocks
    private JwtService jwtService;

    private LoginDetails loginDetails;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", ENCODED_SECRET);
        ReflectionTestUtils.setField(jwtService, "expiry", 180000L);

        loginDetails = new LoginDetails("test@test.com", "Test", "User", "test-user",
                List.of(new SimpleGrantedAuthority("USER")), null, null);
    }

    @Test
    void generateToken_validLoginDetails_returnsNonEmptyJwt() {
        String token = jwtService.generateToken(loginDetails);
        assertNotNull(token);
    }

    @Test
    void generateToken_validLoginDetails_tokenContainsEmailAsSubject() {
        String token = jwtService.generateToken(loginDetails);

        Claims claims = extractClaims(token);
        assertEquals("test@test.com", claims.getSubject());
    }

    @Test
    void generateToken_validLoginDetails_tokenContainsRoleClaim() {
        String token = jwtService.generateToken(loginDetails);

        Claims claims = extractClaims(token);
        assertEquals("USER", claims.get("role"));
    }

    @Test
    void getEmail_validToken_returnsCorrectEmail() {
        String token = jwtService.generateToken(loginDetails);

        String tokenEmail = jwtService.getEmail(token);
        assertEquals("test@test.com", tokenEmail);
    }

    @Test
    void getEmail_malformedToken_throwsJwtException() {
        String token = "malformed-token";
        assertThrows(JwtException.class, () -> jwtService.getEmail(token));
    }

    @Test
    void isTokenValid_matchingEmailAndNonExpiredToken_returnsTrue() {
        String token = jwtService.generateToken(loginDetails);

        Claims claims = extractClaims(token);
        assertEquals("test@test.com", claims.getSubject());
        assertTrue(new Date().getTime() < claims.getExpiration().getTime());
    }

    @Test
    void isTokenValid_emailMismatch_returnsFalse() {
        String token = jwtService.generateToken(loginDetails);

        loginDetails.setEmail("invalid@test.com");
        assertFalse(jwtService.isTokenValid(token, loginDetails));
    }

    @Test
    void isTokenValid_expiredToken_throwsExpiredJwtException() {
        ReflectionTestUtils.setField(jwtService, "expiry", -180000L);
        String token = jwtService.generateToken(loginDetails);

        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenValid(token, loginDetails));
    }

    @Test
    void isTokenValid_emptyToken_returnsFalse() {
        assertFalse(jwtService.isTokenValid("", loginDetails));
    }

    @Test
    void isTokenValid_nullToken_returnsFalse() {
        assertFalse(jwtService.isTokenValid(null, loginDetails));
    }

    private Claims extractClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(ENCODED_SECRET));

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
