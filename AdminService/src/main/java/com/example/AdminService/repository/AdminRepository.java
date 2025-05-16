package com.example.AdminService.repository;

import com.example.AdminService.model.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AdminRepository extends MongoRepository<Admin, UUID> {
    Admin findByUsername(String username);
}
