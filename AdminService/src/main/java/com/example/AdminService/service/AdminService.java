package com.example.AdminService.service;

import com.example.AdminService.model.Admin;
import com.example.AdminService.repository.AdminRepository;
import com.example.AdminService.observer.AdminEventListener;
import com.example.AdminService.security.JwtTokenProvider;
import com.example.AdminService.util.PasswordValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminService {
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    private List<AdminEventListener> listeners = new ArrayList<>();

    @PostConstruct
    public void init() {
        addEventListener(emailService);
        createFirstAdminIfNotExists();
    }

    public void addEventListener(AdminEventListener listener) {
        listeners.add(listener);
    }

    public Admin createAdmin(Admin admin) {
        // Check if username is already taken
        if (adminRepository.findByUsername(admin.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken");
        }

        // Check if email is already taken
        if (adminRepository.findByEmail(admin.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already taken");
        }

        // Validate password strength
        // PasswordValidator.ValidationResult validationResult = PasswordValidator.validatePassword(admin.getPassword());
        // if (!validationResult.isValid()) {
        //     throw new RuntimeException("Password requirements not met: " + String.join(", ", validationResult.getErrors()));
        // }

        // Encode password only if it's not already encoded
        if (!isPasswordEncoded(admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        }
        
        Admin savedAdmin = adminRepository.save(admin);
        notifyAdminCreated(savedAdmin);
        return savedAdmin;
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public Optional<Admin> getAdminById(UUID id) {
        return adminRepository.findById(id);
    }

    public Admin updateAdmin(UUID id, Admin updatedAdmin) {
        Admin existingAdmin = adminRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Admin not found"));

        // Only handle password updates in this method
        if (updatedAdmin.getPassword() != null && !updatedAdmin.getPassword().isEmpty()) {
            throw new RuntimeException("Please use the dedicated password update endpoint");
        }

        return adminRepository.save(existingAdmin);
    }

    public Admin updatePassword(UUID id, String currentPassword, String newPassword) {
        Admin existingAdmin = adminRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Admin not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, existingAdmin.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Validate new password
        PasswordValidator.ValidationResult validationResult = PasswordValidator.validatePassword(newPassword);
        if (!validationResult.isValid()) {
            throw new RuntimeException("Password requirements not met: " + String.join(", ", validationResult.getErrors()));
        }

        existingAdmin.setPassword(passwordEncoder.encode(newPassword));
        notifyAdminPasswordChanged(existingAdmin);
        
        return adminRepository.save(existingAdmin);
    }

    public Admin updateUsername(UUID id, String currentPassword, String newUsername) {
        Admin existingAdmin = adminRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Admin not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, existingAdmin.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Check if new username is already taken
        if (adminRepository.findByUsername(newUsername).isPresent()) {
            throw new RuntimeException("Username is already taken");
        }

        existingAdmin.setUsername(newUsername);
        return adminRepository.save(existingAdmin);
    }

    public Admin updateEmail(UUID id, String currentPassword, String newEmail) {
        Admin existingAdmin = adminRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Admin not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, existingAdmin.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Check if new email is already taken
        if (adminRepository.findByEmail(newEmail).isPresent()) {
            throw new RuntimeException("Email is already taken");
        }

        existingAdmin.setEmail(newEmail);
        return adminRepository.save(existingAdmin);
    }

    public void deleteAdmin(UUID id) {
        Admin admin = adminRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Admin not found"));
        adminRepository.deleteById(id);
        notifyAdminDeleted(admin);
    }

    public boolean authenticate(String username, String password) {
        return adminRepository.findByUsername(username)
            .map(admin -> passwordEncoder.matches(password, admin.getPassword()))
            .orElse(false);
    }

    private void notifyAdminCreated(Admin admin) {
        for (AdminEventListener listener : listeners) {
            listener.onAdminCreated(admin);
        }
    }

    private void notifyAdminDeleted(Admin admin) {
        for (AdminEventListener listener : listeners) {
            listener.onAdminDeleted(admin);
        }
    }

    private void notifyAdminPasswordChanged(Admin admin) {
        for (AdminEventListener listener : listeners) {
            listener.onAdminPasswordChanged(admin);
        }
    }

    private void createFirstAdminIfNotExists() {
        if (!adminRepository.findByUsername("firstadmin").isPresent()) {
            Admin firstadmin = new Admin();
            firstadmin.setUsername("firstadmin");
            // Don't encode the password here since createAdmin will handle it
            firstadmin.setPassword("aAcjdcen1938@@$$$$@@gsvc-DDDH");
            firstadmin.setEmail("firstadmin@gmail.com");
            createAdmin(firstadmin);
        }
    }

    private boolean isPasswordEncoded(String password) {
        // BCrypt passwords start with $2a$, $2b$, or $2y$
        return password.matches("^\\$2[aby]\\$\\d{2}\\$.*");
    }

    public void initiatePasswordReset(String email) {
        Admin admin = adminRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Admin not found"));

        // Create a special JWT token for password reset
        String resetToken = jwtTokenProvider.createPasswordResetToken(admin.getUsername());

        // Send reset email
        String resetLink = "http://your-frontend-url/reset-password?token=" + resetToken;
        emailService.sendPasswordResetEmail(admin.getEmail(), resetLink);
    }

    public Admin resetPassword(String token, String newPassword) {
        // Verify token and get username
        String username = jwtTokenProvider.validatePasswordResetTokenAndGetUsername(token);
        if (username == null) {
            throw new RuntimeException("Invalid or expired token");
        }

        // Validate new password
        PasswordValidator.ValidationResult validationResult = PasswordValidator.validatePassword(newPassword);
        if (!validationResult.isValid()) {
            throw new RuntimeException("Password requirements not met: " + String.join(", ", validationResult.getErrors()));
        }

        // Update password
        Admin admin = adminRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Admin not found"));

        admin.setPassword(passwordEncoder.encode(newPassword));
        Admin savedAdmin = adminRepository.save(admin);

        // Notify admin
        notifyAdminPasswordChanged(savedAdmin);
        
        return savedAdmin;
    }
}
