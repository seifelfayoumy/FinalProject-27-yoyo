package com.example.AdminService.repository;

import com.example.AdminService.model.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface AdminRepository extends MongoRepository<Admin, UUID> {
    Optional<Admin> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<Admin> findByEmail(String email);
    boolean existsByEmail(String email);
}