package com.example.TransactionService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients   // ← turn on Feign
public class TransactionServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(TransactionServiceApplication.class, args);
	}
}
