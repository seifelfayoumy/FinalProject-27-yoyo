package com.example.AdminService.config;

import com.example.AdminService.model.Admin;
import com.example.AdminService.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private AdminService adminService;

    @Override
    public void run(String... args) {
        // Create default admin if it doesn't exist
        if (!adminService.getAllAdmins().stream().anyMatch(a -> "firstadmin".equals(a.getUsername()))) {
            Admin admin = new Admin();
            admin.setUsername("firstadmin");
            admin.setPassword("Acjdcen1938@@$$$$@@gsvc-DDDH"); // Password will be encoded by AdminService
            admin.setEmail("firstadmin@gmail.com");
            try {
                adminService.createAdmin(admin);
                System.out.println("Default admin account created");
            } catch (Exception e) {
                System.err.println("Failed to create default admin: " + e.getMessage());
            }
        }
    }
} 