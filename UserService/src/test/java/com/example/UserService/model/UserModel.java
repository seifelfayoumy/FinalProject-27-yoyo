package com.example.UserService.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class UserModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    private String fullName;
    
    @Column(name = "is_email_verified")
    private Boolean isEmailVerified = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Manual implementation of Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    // Create a builder from existing UserModel (equivalent to toBuilder)
    public Builder toBuilder() {
        Builder builder = new Builder();
        builder.id = this.id;
        builder.email = this.email;
        builder.password = this.password;
        builder.fullName = this.fullName;
        builder.isEmailVerified = this.isEmailVerified;
        builder.createdAt = this.createdAt;
        builder.updatedAt = this.updatedAt;
        return builder;
    }
    
    // The Builder implementation with validation
    public static class Builder {
        private Long id;
        private String email;
        private String password;
        private String fullName;
        private Boolean isEmailVerified = false;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        private Builder() {
            // Private constructor to enforce the use of builder() method
        }
        
        public Builder email(String email) {
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Email cannot be null or empty");
            }
            this.email = email;
            return this;
        }
        
        public Builder password(String password) {
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("Password cannot be null or empty");
            }
            this.password = password;
            return this;
        }
        
        public Builder id(Long id) {
            this.id = id;
            return this;
        }
        
        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }
        
        public Builder isEmailVerified(Boolean isEmailVerified) {
            this.isEmailVerified = isEmailVerified;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public UserModel build() {
            // Validate required fields
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Email is required");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("Password is required");
            }
            
            UserModel user = new UserModel();
            user.setId(this.id);
            user.setEmail(this.email);
            user.setPassword(this.password);
            user.setFullName(this.fullName);
            user.setIsEmailVerified(this.isEmailVerified != null ? this.isEmailVerified : false);
            user.setCreatedAt(this.createdAt);
            user.setUpdatedAt(this.updatedAt);
            return user;
        }
    }
}
