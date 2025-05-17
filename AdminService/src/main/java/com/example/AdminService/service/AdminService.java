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
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
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

        existingAdmin.setUsername(updatedAdmin.getUsername());
        existingAdmin.setEmail(updatedAdmin.getEmail());

        if (updatedAdmin.getPassword() != null && !updatedAdmin.getPassword().isEmpty()) {
            existingAdmin.setPassword(passwordEncoder.encode(updatedAdmin.getPassword()));
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
            firstadmin.setPassword(passwordEncoder.encode("admin123"));
            firstadmin.setEmail("firstadmin@gmail.com");
            adminRepository.save(firstadmin);
        }
    }
}