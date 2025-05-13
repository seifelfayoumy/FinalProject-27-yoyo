package com.example.UserService.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Allow these endpoints without authentication
                .requestMatchers("/api/users/test").permitAll()
                .requestMatchers("/api/users/login").permitAll()
                .requestMatchers("/api/users/verify").permitAll()
                .requestMatchers("/api/users/request-password-reset").permitAll()
                .requestMatchers("/api/users/reset-password").permitAll()
                // For testing purposes, allow all endpoints without authentication
                // In production, you would comment this out and uncomment the line below
                .requestMatchers("/**").permitAll()
                // .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
            
        return http.build();
    }
} 