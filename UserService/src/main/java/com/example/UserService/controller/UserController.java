package com.example.UserService.controller;

import com.example.UserService.model.UserModel;
import com.example.UserService.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.status(HttpStatus.OK).body("success");
    }

    @PostMapping("")
    public ResponseEntity<?> createUser(@RequestBody UserModel user) {
        try {
            UserModel createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            // Log the full stack trace
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserModel> getUserById(@PathVariable Long id) {
        UserModel user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserModel> updateUser(@PathVariable Long id, @RequestBody UserModel user) {
        UserModel updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");
        
        if (email == null || password == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Email and password are required");
            return ResponseEntity.badRequest().body(response);
        }
        
        Map<String, Object> loginResponse = userService.login(email, password);
        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam String token) {
        Map<String, Object> response = new HashMap<>();
        
        boolean verified = userService.verifyEmail(token);
        
        if (verified) {
            response.put("success", true);
            response.put("message", "Email verified successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Invalid or expired token");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<Map<String, Object>> requestPasswordReset(@RequestBody Map<String, String> requestData) {
        String email = requestData.get("email");
        
        if (email == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Email is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        // We don't want to reveal if the email exists or not
        try {
            userService.requestPasswordReset(email);
        } catch (Exception e) {
            // Log the error but return success response
            System.err.println("Error requesting password reset: " + e.getMessage());
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "If your email is registered, you will receive a password reset link");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> resetData) {
        String token = resetData.get("token");
        String newPassword = resetData.get("newPassword");
        
        if (token == null || newPassword == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Token and new password are required");
            return ResponseEntity.badRequest().body(response);
        }
        
        boolean reset = userService.resetPassword(token, newPassword);
        
        Map<String, Object> response = new HashMap<>();
        if (reset) {
            response.put("success", true);
            response.put("message", "Password reset successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Invalid or expired token");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        // This endpoint will only be accessible if the JWT token is valid
        // The JWT filter will extract the email from the token and set it in the security context
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Optional<UserModel> userOpt = userService.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        UserModel user = userOpt.get();
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("fullName", user.getFullName());
        response.put("isEmailVerified", user.getIsEmailVerified());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reset-password")
    public ResponseEntity<String> getResetPasswordForm(@RequestParam String token) {
        // Check if token is valid
        if (!userService.isValidPasswordResetToken(token)) {
            return ResponseEntity.badRequest().body("<html><body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                    "<h1 style='color: #d9534f;'>Invalid or expired token</h1>" +
                    "<p>The password reset link has expired or is invalid. Please request a new password reset link.</p>" +
                    "<a href='/api/users/request-password-reset' style='display: inline-block; background-color: #337ab7; color: white; padding: 10px 15px; text-decoration: none; border-radius: 4px;'>Request New Reset Link</a>" +
                    "</body></html>");
        }
        
        // Create a simple HTML form for password reset with better styling
        String html = "<!DOCTYPE html>" +
                "<html><head><title>Reset Your Password</title>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "h1 { color: #337ab7; }" +
                ".form-group { margin-bottom: 15px; }" +
                "label { display: block; margin-bottom: 5px; }" +
                "input[type='password'] { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; }" +
                "button { background-color: #337ab7; color: white; border: none; padding: 10px 15px; border-radius: 4px; cursor: pointer; }" +
                "button:hover { background-color: #286090; }" +
                ".success { color: #5cb85c; }" +
                ".error { color: #d9534f; }" +
                "</style></head>" +
                "<body>" +
                "<h1>Reset Your Password</h1>" +
                "<p>Please enter your new password below:</p>" +
                "<form id='resetForm' onsubmit='submitForm(); return false;'>" +
                "<input type='hidden' id='token' value='" + token + "' />" +
                "<div class='form-group'>" +
                "<label for='password'>New Password:</label>" +
                "<input type='password' id='password' required />" +
                "</div>" +
                "<div class='form-group'>" +
                "<label for='confirmPassword'>Confirm Password:</label>" +
                "<input type='password' id='confirmPassword' required />" +
                "</div>" +
                "<div class='form-group'>" +
                "<button type='submit'>Reset Password</button>" +
                "</div>" +
                "</form>" +
                "<div id='message'></div>" +
                "<script>" +
                "function submitForm() {" +
                "  const password = document.getElementById('password').value;" +
                "  const confirmPassword = document.getElementById('confirmPassword').value;" +
                "  const token = document.getElementById('token').value;" +
                "  const messageDiv = document.getElementById('message');" +
                "  " +
                "  if (password !== confirmPassword) {" +
                "    messageDiv.innerHTML = '<p class=\"error\">Passwords do not match</p>';" +
                "    return;" +
                "  }" +
                "  " +
                "  fetch('/api/users/reset-password', {" +
                "    method: 'POST'," +
                "    headers: { 'Content-Type': 'application/json' }," +
                "    body: JSON.stringify({ token: token, newPassword: password })" +
                "  })" +
                "  .then(response => response.json())" +
                "  .then(data => {" +
                "    if (data.success) {" +
                "      messageDiv.innerHTML = '<p class=\"success\">Password reset successful! You can now login with your new password.</p>';" +
                "      document.getElementById('resetForm').style.display = 'none';" +
                "    } else {" +
                "      messageDiv.innerHTML = '<p class=\"error\">Error: ' + data.message + '</p>';" +
                "    }" +
                "  })" +
                "  .catch(error => {" +
                "    messageDiv.innerHTML = '<p class=\"error\">An error occurred. Please try again.</p>';" +
                "    console.error('Error:', error);" +
                "  });" +
                "}" +
                "</script>" +
                "</body></html>";
        
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    @GetMapping("/request-password-reset")
    public ResponseEntity<String> getRequestPasswordResetForm() {
        String html = "<!DOCTYPE html>" +
                "<html><head><title>Request Password Reset</title>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "h1 { color: #337ab7; }" +
                ".form-group { margin-bottom: 15px; }" +
                "label { display: block; margin-bottom: 5px; }" +
                "input[type='email'] { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; }" +
                "button { background-color: #337ab7; color: white; border: none; padding: 10px 15px; border-radius: 4px; cursor: pointer; }" +
                "button:hover { background-color: #286090; }" +
                ".success { color: #5cb85c; }" +
                ".error { color: #d9534f; }" +
                "</style></head>" +
                "<body>" +
                "<h1>Reset Your Password</h1>" +
                "<p>Enter your email address below, and we'll send you a link to reset your password:</p>" +
                "<form id='resetRequestForm' onsubmit='submitForm(); return false;'>" +
                "<div class='form-group'>" +
                "<label for='email'>Email Address:</label>" +
                "<input type='email' id='email' required />" +
                "</div>" +
                "<div class='form-group'>" +
                "<button type='submit'>Request Reset Link</button>" +
                "</div>" +
                "</form>" +
                "<div id='message'></div>" +
                "<script>" +
                "function submitForm() {" +
                "  const email = document.getElementById('email').value;" +
                "  const messageDiv = document.getElementById('message');" +
                "  " +
                "  fetch('/api/users/request-password-reset', {" +
                "    method: 'POST'," +
                "    headers: { 'Content-Type': 'application/json' }," +
                "    body: JSON.stringify({ email: email })" +
                "  })" +
                "  .then(response => response.json())" +
                "  .then(data => {" +
                "    if (data.success) {" +
                "      messageDiv.innerHTML = '<p class=\"success\">If your email address is registered with us, you will receive a password reset link shortly.</p>';" +
                "      document.getElementById('resetRequestForm').style.display = 'none';" +
                "    } else {" +
                "      messageDiv.innerHTML = '<p class=\"error\">Error: ' + data.message + '</p>';" +
                "    }" +
                "  })" +
                "  .catch(error => {" +
                "    messageDiv.innerHTML = '<p class=\"error\">An error occurred. Please try again.</p>';" +
                "    console.error('Error:', error);" +
                "  });" +
                "}" +
                "</script>" +
                "</body></html>";
        
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}
