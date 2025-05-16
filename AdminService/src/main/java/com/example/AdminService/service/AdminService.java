package com.example.AdminService.service;

import com.example.AdminService.model.Admin;
import com.example.AdminService.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    public Admin createAdmin(Admin admin) {
        return adminRepository.save(admin);
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public Optional<Admin> getAdminById(UUID id) {
        return adminRepository.findById(id);
    }

    public Admin updateAdmin(UUID id, Admin updatedAdmin) {
        updatedAdmin.setId(id);
        return adminRepository.save(updatedAdmin);
    }

    public void deleteAdmin(UUID id) {
        adminRepository.deleteById(id);
    }

    public boolean authenticate(String username, String password) {
        Admin admin = adminRepository.findByUsername(username);
        return admin != null && admin.getPassword().equals(password);
    }
}
