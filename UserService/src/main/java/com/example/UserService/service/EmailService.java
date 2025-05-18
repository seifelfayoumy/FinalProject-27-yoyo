package com.example.UserService.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8000}")
    private String baseUrl;

    public void sendVerificationEmail(String toEmail, String token) throws MessagingException {
        String verificationLink = baseUrl + "/api/users/verify?token=" + token;

        String htmlContent = "<html><body>" +
                "<h2>Email Verification</h2>" +
                "<p>Hello,</p>" +
                "<p>Thank you for registering with us. Please click the link below to verify your email address:</p>" +
                "<p><a href=\"" + verificationLink + "\">Verify Email</a></p>" +
                "<p>Or copy and paste the following URL into your browser:</p>" +
                "<p>" + verificationLink + "</p>" +
                "<p>This link will expire in 24 hours.</p>" +
                "<p>Best regards,<br>The Walmart Team</p>" +
                "</body></html>";

        sendHtmlEmail(toEmail, "Verify Your Email Address", htmlContent);
    }

    public void sendPasswordResetEmail(String toEmail, String token) throws MessagingException {
        String resetLink = baseUrl + "/api/users/reset-password?token=" + token;

        String htmlContent = "<html><body>" +
                "<h2>Password Reset</h2>" +
                "<p>Hello,</p>" +
                "<p>You have requested to reset your password. Please click the link below to reset your password:</p>" +
                "<p><a href=\"" + resetLink + "\">Reset Password</a></p>" +
                "<p>Or copy and paste the following URL into your browser:</p>" +
                "<p>" + resetLink + "</p>" +
                "<p>This link will expire in 30 minutes.</p>" +
                "<p>If you did not request a password reset, please ignore this email or contact support.</p>" +
                "<p>Best regards,<br>The Walmart Team</p>" +
                "</body></html>";

        sendHtmlEmail(toEmail, "Reset Your Password", htmlContent);
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Email sent successfully to: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("Failed to send email to " + toEmail + ": " + e.getMessage());
            // For Gmail auth issues, provide a more helpful message
            if (e.getMessage() != null && e.getMessage().contains("Authentication failed")) {
                System.err.println("Gmail authentication failed. If using Gmail, you likely need to use an App Password. " +
                        "See https://support.google.com/accounts/answer/185833 for more information.");
            }
            throw e;
        }
    }
}