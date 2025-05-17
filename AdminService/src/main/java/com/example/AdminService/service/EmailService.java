package com.example.AdminService.service;

import com.example.AdminService.model.Admin;
import com.example.AdminService.observer.AdminEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService implements AdminEventListener {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void onAdminCreated(Admin admin) {
        sendEmail(
            admin.getEmail(),
            "Welcome to Walmart Admin System",
            "Dear " + admin.getUsername() + ",\n\n" +
            "Your admin account has been created successfully.\n" +
            "Please keep your credentials safe.\n\n" +
            "Best regards,\nWalmart Admin Team"
        );
    }

    @Override
    public void onAdminDeleted(Admin admin) {
        sendEmail(
            admin.getEmail(),
            "Walmart Admin Account Deleted",
            "Dear " + admin.getUsername() + ",\n\n" +
            "Your admin account has been deleted.\n" +
            "If this was not requested by you, please contact support immediately.\n\n" +
            "Best regards,\nWalmart Admin Team"
        );
    }

    @Override
    public void onAdminPasswordChanged(Admin admin) {
        sendEmail(
            admin.getEmail(),
            "Walmart Admin Password Changed",
            "Dear " + admin.getUsername() + ",\n\n" +
            "Your admin account password has been changed.\n" +
            "If this was not done by you, please contact support immediately.\n\n" +
            "Best regards,\nWalmart Admin Team"
        );
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
} 