package com.example.AdminService.service;

import com.example.AdminService.model.Admin;
import com.example.AdminService.repository.AdminRepository;
import com.example.AdminService.observer.AdminEventListener;
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

        // Encode password only if it's not already encoded
        if (!isPasswordEncoded(admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        }
        
        Admin savedAdmin = adminRepository.save(admin);
        notifyAdminCreated(savedAdmin);
        return savedAdmin;
    }

    // Helper method to check if a password is already encoded
    private boolean isPasswordEncoded(String password) {
        // BCrypt passwords start with $2a$, $2b$, or $2y$
        return password.matches("^\\$2[aby]\\$\\d{2}\\$.*");
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

        // Check if new username is already taken by another admin
        if (!existingAdmin.getUsername().equals(updatedAdmin.getUsername()) &&
            adminRepository.findByUsername(updatedAdmin.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken");
        }

        // Check if new email is already taken by another admin
        if (!existingAdmin.getEmail().equals(updatedAdmin.getEmail()) &&
            adminRepository.findByEmail(updatedAdmin.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already taken");
        }

        existingAdmin.setUsername(updatedAdmin.getUsername());
        existingAdmin.setEmail(updatedAdmin.getEmail());

        if (updatedAdmin.getPassword() != null && !updatedAdmin.getPassword().isEmpty()) {
            if (!isPasswordEncoded(updatedAdmin.getPassword())) {
                existingAdmin.setPassword(passwordEncoder.encode(updatedAdmin.getPassword()));
            } else {
                existingAdmin.setPassword(updatedAdmin.getPassword());
            }
            notifyAdminPasswordChanged(existingAdmin);
        }

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
            firstadmin.setPassword("admin123");
            firstadmin.setEmail("firstadmin@gmail.com");
            createAdmin(firstadmin);
        }
    }
}
