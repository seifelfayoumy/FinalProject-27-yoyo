package com.example.AdminService.model;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "Admins")
public class Admin {
    @Id
    private UUID id;
    private String username;
    private String password;

    //Constructors
    //Getters and Setters
}
