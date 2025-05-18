package com.example.AdminService.config;

import com.example.AdminService.model.Admin;
import com.example.AdminService.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create default admin if it doesn't exist
        if (!adminRepository.existsByUsername("firstadmin")) {
            Admin admin = new Admin();
            admin.setUsername("firstadmin");
            admin.setPassword(passwordEncoder.encode("admin123")); // You should change this password
            admin.setEmail("firstadmin@gmail.com");
            adminRepository.save(admin);
            System.out.println("Default admin account created");
        }
    }
} 