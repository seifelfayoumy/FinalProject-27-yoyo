package com.example.AdminService.service;

import com.example.AdminService.model.Admin;
import com.example.AdminService.model.Product;
import com.example.AdminService.observer.AdminEventListener;
import com.example.AdminService.observer.ProductStockEventListener;
import com.example.AdminService.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService implements AdminEventListener, ProductStockEventListener {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private AdminRepository adminRepository;

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

    @Override
    public void onLowStock(Product product, int currentStock, int threshold) {
        List<Admin> admins = adminRepository.findAll();
        String subject = "Walmart Product Stock Alert: " + product.getName();
        
        for (Admin admin : admins) {
            String message = String.format(
                "Dear %s,\n\n" +
                "This is to inform you that the following product is running low on stock:\n\n" +
                "Product Name: %s\n" +
                "Current Stock Level: %d\n" +
                "Stock Threshold: %d\n\n" +
                "Please take necessary action to replenish the inventory.\n\n" +
                "Best regards,\n" +
                "Walmart Inventory System",
                admin.getUsername(), product.getName(), currentStock, threshold
            );
            
            sendEmail(admin.getEmail(), subject, message);
        }
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
} 