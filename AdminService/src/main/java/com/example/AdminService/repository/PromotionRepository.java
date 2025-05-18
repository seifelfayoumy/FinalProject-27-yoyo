package com.example.AdminService.repository;

import com.example.AdminService.model.Promotion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromotionRepository extends MongoRepository<Promotion, UUID> {
    Optional<Promotion> findByName(String name);
}
