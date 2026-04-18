package com.zedapps.bookshare.service.auth;

import com.zedapps.bookshare.dto.login.LoginDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Objects;

/**
 * @author smzoha
 * @since 18/4/26
 **/
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiry.ms}")
    private long expiry;

    public String generateToken(LoginDetails loginDetails) {
        return Jwts.builder()
                .subject(loginDetails.getEmail())
                .claim("role", loginDetails.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .findFirst()
                        .orElse(""))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiry))
                .signWith(getSigningKey())
                .compact();
    }

    public String getEmail(String token) {
        return extractClaim(token).getSubject();
    }

    public boolean isTokenValid(String token, LoginDetails loginDetails) {
        return !StringUtils.isEmpty(token)
                && Objects.equals(getEmail(token), loginDetails.getEmail())
                && extractClaim(token).getExpiration().after(new Date());
    }

    private Claims extractClaim(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }
}
