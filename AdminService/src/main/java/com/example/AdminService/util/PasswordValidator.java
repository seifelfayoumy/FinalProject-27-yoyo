package com.example.AdminService.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PasswordValidator {
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 32;
    private static final Pattern HAS_UPPER = Pattern.compile("[A-Z]");
    private static final Pattern HAS_LOWER = Pattern.compile("[a-z]");
    private static final Pattern HAS_SPECIAL = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");
    
    public static class ValidationResult {
        private boolean valid;
        private List<String> errors;

        public ValidationResult() {
            this.valid = true;
            this.errors = new ArrayList<>();
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void addError(String error) {
            this.valid = false;
            this.errors.add(error);
        }
    }

    public static ValidationResult validatePassword(String password) {
        ValidationResult result = new ValidationResult();

        if (password == null || password.isEmpty()) {
            result.addError("Password cannot be empty");
            return result;
        }

        if (password.length() < MIN_LENGTH) {
            result.addError("Password must be at least " + MIN_LENGTH + " characters long");
        }

        if (password.length() > MAX_LENGTH) {
            result.addError("Password cannot be longer than " + MAX_LENGTH + " characters");
        }

        if (!HAS_UPPER.matcher(password).find()) {
            result.addError("Password must contain at least one uppercase letter");
        }

        if (!HAS_LOWER.matcher(password).find()) {
            result.addError("Password must contain at least one lowercase letter");
        }

        if (!HAS_SPECIAL.matcher(password).find()) {
            result.addError("Password must contain at least one special character");
        }

        return result;
    }
} 