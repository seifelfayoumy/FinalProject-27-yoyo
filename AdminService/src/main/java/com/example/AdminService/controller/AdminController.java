package com.example.AdminService.controller;

import com.example.AdminService.model.Admin;
import com.example.AdminService.security.JwtTokenProvider;
import com.example.AdminService.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DuplicateKeyException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createAdmin(@RequestBody Admin admin) {
        try {
            Admin savedAdmin = adminService.createAdmin(admin);
            return ResponseEntity.ok(savedAdmin);
        } catch (DuplicateKeyException e) {
            return ResponseEntity.badRequest()
                .body("Username or email already exists");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error creating admin: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        try {
            if (adminService.authenticate(username, password)) {
                String token = jwtTokenProvider.createToken(username);
                
                Map<String, Object> response = new HashMap<>();
                response.put("username", username);
                response.put("token", token);
                
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(401).body("Invalid username/password");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error during login: " + e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllAdmins() {
        try {
            return ResponseEntity.ok(adminService.getAllAdmins());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error fetching admins: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAdminById(@PathVariable UUID id) {
        try {
            Optional<Admin> admin = adminService.getAdminById(id);
            return admin.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error fetching admin: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateAdmin(@PathVariable UUID id, @RequestBody Admin admin) {
        try {
            return ResponseEntity.ok(adminService.updateAdmin(id, admin));
        } catch (DuplicateKeyException e) {
            return ResponseEntity.badRequest()
                .body("Username or email already exists");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                .body("Admin not found");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error updating admin: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAdmin(@PathVariable UUID id) {
        try {
            adminService.deleteAdmin(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                .body("Admin not found");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error deleting admin: " + e.getMessage());
        }
    }
}