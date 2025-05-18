package com.example.AdminService.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long validityInMilliseconds;

    private Key key;

    @PostConstruct
    protected void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(String username) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", Collections.singletonList("ROLE_ADMIN"));
        
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new JwtException("Session expired. Please login again.");
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("Invalid token. Please login again.");
        }
    }

    public String getUsername(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            // Even if token is expired, we can still extract the username
            return e.getClaims().getSubject();
        }
    }

    public Authentication getAuthentication(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            List<String> roles = claims.get("roles", List.class);
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            UserDetails userDetails = User.builder()
                    .username(claims.getSubject())
                    .password("")
                    .authorities(authorities)
                    .build();

            return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
        } catch (ExpiredJwtException e) {
            throw new JwtException("Session expired. Please login again.");
        }
    }

    public String createPasswordResetToken(String username) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("type", "password_reset");
        
        Date now = new Date();
        // Password reset tokens expire in 10 minutes
        Date validity = new Date(now.getTime() + 600000);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String validatePasswordResetTokenAndGetUsername(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Verify this is a password reset token
            if (!"password_reset".equals(claims.get("type"))) {
                return null;
            }

            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            throw new JwtException("Password reset link has expired. Please request a new one.");
        } catch (JwtException e) {
            return null;
        }
    }
} 