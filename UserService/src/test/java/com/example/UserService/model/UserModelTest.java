package com.example.UserService.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserModelTest {

    @Test
    public void testBuilderWithRequiredAndOptionalFields() {
        // Creating a user with only required fields (email and password)
        UserModel minimalUser = UserModel.builder()
                .email("minimal@example.com")
                .password("password123")
                .build();
        
        // Verify required fields are set
        assertEquals("minimal@example.com", minimalUser.getEmail());
        assertEquals("password123", minimalUser.getPassword());
        
        // Verify optional fields have default values
        assertNull(minimalUser.getFullName());
        assertFalse(minimalUser.getIsEmailVerified());
        
        // Creating a user with all fields
        UserModel completeUser = UserModel.builder()
                .email("complete@example.com")
                .password("password456")
                .fullName("Complete User")
                .isEmailVerified(true)
                .build();
        
        // Verify all fields are set correctly
        assertEquals("complete@example.com", completeUser.getEmail());
        assertEquals("password456", completeUser.getPassword());
        assertEquals("Complete User", completeUser.getFullName());
        assertTrue(completeUser.getIsEmailVerified());
    }
    
    @Test
    public void testToBuilderModifiesOnlySpecifiedFields() {
        // Create initial user
        UserModel originalUser = UserModel.builder()
                .email("original@example.com")
                .password("original123")
                .fullName("Original User")
                .build();
        
        // Update only the email using toBuilder
        UserModel updatedUser = originalUser.toBuilder()
                .email("updated@example.com")
                .build();
        
        // Verify only the email was updated
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals("original123", updatedUser.getPassword());
        assertEquals("Original User", updatedUser.getFullName());
    }
    
    @Test
    public void testValidationOfRequiredFields() {
        // Test that null email throws exception
        Exception emailException = assertThrows(IllegalArgumentException.class, () -> {
            UserModel.builder()
                    .email(null)
                    .password("password123")
                    .build();
        });
        assertEquals("Email cannot be null or empty", emailException.getMessage());
        
        // Test that empty email throws exception
        Exception emptyEmailException = assertThrows(IllegalArgumentException.class, () -> {
            UserModel.builder()
                    .email("  ")
                    .password("password123")
                    .build();
        });
        assertEquals("Email cannot be null or empty", emptyEmailException.getMessage());
        
        // Test that null password throws exception
        Exception passwordException = assertThrows(IllegalArgumentException.class, () -> {
            UserModel.builder()
                    .email("test@example.com")
                    .password(null)
                    .build();
        });
        assertEquals("Password cannot be null or empty", passwordException.getMessage());
        
        // Test that missing required fields throw exceptions at build time
        Exception missingEmailException = assertThrows(IllegalArgumentException.class, () -> {
            UserModel.builder()
                    .build();
        });
        assertEquals("Email is required", missingEmailException.getMessage());
        
        Exception missingPasswordException = assertThrows(IllegalArgumentException.class, () -> {
            UserModel.builder()
                    .email("test@example.com")
                    .build();
        });
        assertEquals("Password is required", missingPasswordException.getMessage());
    }
} 