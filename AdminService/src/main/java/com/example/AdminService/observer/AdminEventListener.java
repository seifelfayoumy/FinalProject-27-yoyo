package com.example.AdminService.observer;

import com.example.AdminService.model.Admin;

public interface AdminEventListener {
    void onAdminCreated(Admin admin);
    void onAdminDeleted(Admin admin);
    void onAdminPasswordChanged(Admin admin);
} 