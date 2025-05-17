package com.example.UserService.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long validityInMilliseconds;

    private Key key;

    @PostConstruct
    protected void init() {
        // Convert the secret key to a Key object
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String createToken(String email, Long userId) {
        return createToken(email, userId, null, validityInMilliseconds);
    }

    public String createEmailVerificationToken(String email, Long userId) {
        // Token valid for 24 hours
        long emailVerificationValidity = 24 * 60 * 60 * 1000;
        Map<String, Object> claims = new HashMap<>();
        claims.put("purpose", "email_verification");
        return createToken(email, userId, claims, emailVerificationValidity);
    }

    public String createPasswordResetToken(String email, Long userId) {
        // Token valid for 30 minutes
        long passwordResetValidity = 30 * 60 * 1000;
        Map<String, Object> claims = new HashMap<>();
        claims.put("purpose", "password_reset");
        return createToken(email, userId, claims, passwordResetValidity);
    }

    private String createToken(String email, Long userId, Map<String, Object> additionalClaims, long validity) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("userId", userId);

        if (additionalClaims != null) {
            claims.putAll(additionalClaims);
        }

        Date now = new Date();
        Date validityDate = new Date(now.getTime() + validity);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validityDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Long getUserIdFromToken(String token) {
        return ((Number) getAllClaimsFromToken(token).get("userId")).longValue();
    }

    public String getPurposeFromToken(String token) {
        return (String) getAllClaimsFromToken(token).get("purpose");
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validateTokenForPurpose(String token, String purpose) {
        if (!validateToken(token)) {
            return false;
        }

        String tokenPurpose = getPurposeFromToken(token);
        return purpose.equals(tokenPurpose);
    }

    private boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
}