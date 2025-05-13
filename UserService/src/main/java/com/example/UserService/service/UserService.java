package com.example.UserService.service;

import com.example.UserService.model.UserModel;
import com.example.UserService.repository.UserRepository;
import com.example.UserService.security.JwtTokenProvider;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    
    @Transactional
    public UserModel createUser(UserModel user) {
        // Validate user data
        if (user.getEmail() == null || user.getPassword() == null) {
            throw new IllegalArgumentException("Email and password are required");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        
        // Encrypt password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Set default email verification status
        user.setIsEmailVerified(false);
        
        // Save user to get the ID
        UserModel savedUser = userRepository.save(user);
        
        // Generate verification token using JWT
        String token = jwtTokenProvider.createEmailVerificationToken(user.getEmail(), savedUser.getId());
        
        // Send verification email
        try {
            emailService.sendVerificationEmail(user.getEmail(), token);
        } catch (MessagingException e) {
            // Log the error but continue - we don't want to roll back the user creation
            System.err.println("Failed to send verification email: " + e.getMessage());
        }
        
        return savedUser;
    }
    
    public UserModel getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }
    
    @Transactional
    public UserModel updateUser(Long id, UserModel userDetails) {
        UserModel existingUser = getUserById(id);
        
        // Update only non-null fields
        if (userDetails.getFullName() != null) {
            existingUser.setFullName(userDetails.getFullName());
        }
        
        // If updating email, reset verification status
        if (userDetails.getEmail() != null && !userDetails.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(userDetails.getEmail())) {
                throw new IllegalArgumentException("Email already in use");
            }
            existingUser.setEmail(userDetails.getEmail());
            existingUser.setIsEmailVerified(false);
            
            // Generate new verification token using JWT
            String token = jwtTokenProvider.createEmailVerificationToken(userDetails.getEmail(), existingUser.getId());
            
            // Send verification email
            try {
                emailService.sendVerificationEmail(userDetails.getEmail(), token);
            } catch (MessagingException e) {
                // Log the error but continue
                System.err.println("Failed to send verification email: " + e.getMessage());
            }
        }
        
        // If updating password, encrypt it
        if (userDetails.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        
        return userRepository.save(existingUser);
    }
    
    @Transactional
    public void deleteUser(Long id) {
        UserModel user = getUserById(id);
        userRepository.delete(user);
    }
    
    public Map<String, Object> login(String email, String password) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<UserModel> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        UserModel user = userOpt.get();
        
        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        // Generate JWT token
        String token = jwtTokenProvider.createToken(user.getEmail(), user.getId());
        
        response.put("success", true);
        response.put("token", token);
        response.put("userId", user.getId());
        
        return response;
    }
    
    // JWT tokens are stateless, so logout is handled on the client side
    // by removing the token. This method is kept for API compatibility.
    public void logout(String token) {
        // No server-side action needed for JWT
    }
    
    @Transactional
    public boolean verifyEmail(String token) {
        // Validate token and check if it's for email verification purpose
        if (!jwtTokenProvider.validateTokenForPurpose(token, "email_verification")) {
            return false;
        }
        
        String email = jwtTokenProvider.getEmailFromToken(token);
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        
        Optional<UserModel> userOpt = userRepository.findById(userId);
        
        if (userOpt.isEmpty() || !userOpt.get().getEmail().equals(email)) {
            return false;
        }
        
        UserModel user = userOpt.get();
        user.setIsEmailVerified(true);
        userRepository.save(user);
        
        return true;
    }
    
    @Transactional
    public void requestPasswordReset(String email) {
        Optional<UserModel> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // We don't want to reveal whether an email exists or not for security reasons
            // Just return without error, but don't send email
            return;
        }
        
        UserModel user = userOpt.get();
        
        // Generate password reset token using JWT
        String token = jwtTokenProvider.createPasswordResetToken(email, user.getId());
        
        // Send password reset email
        try {
            emailService.sendPasswordResetEmail(email, token);
        } catch (MessagingException e) {
            System.err.println("Failed to send password reset email: " + e.getMessage());
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
    
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        // Validate token and check if it's for password reset purpose
        if (!jwtTokenProvider.validateTokenForPurpose(token, "password_reset")) {
            return false;
        }
        
        String email = jwtTokenProvider.getEmailFromToken(token);
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        
        Optional<UserModel> userOpt = userRepository.findById(userId);
        
        if (userOpt.isEmpty() || !userOpt.get().getEmail().equals(email)) {
            return false;
        }
        
        UserModel user = userOpt.get();
        
        // Encrypt new password
        user.setPassword(passwordEncoder.encode(newPassword));
        
        // Save updated user
        userRepository.save(user);
        
        return true;
    }
}
