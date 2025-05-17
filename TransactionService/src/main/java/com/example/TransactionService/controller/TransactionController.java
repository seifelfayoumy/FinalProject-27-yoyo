package com.example.TransactionService.controller;

import com.example.TransactionService.model.Transaction;
import com.example.TransactionService.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> create(@RequestBody Transaction tx) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(tx));
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    public Transaction get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public List<Transaction> list() {
        return service.list();
    }

    @PutMapping("/{id}")
    public Transaction update(@PathVariable Long id,
                              @RequestBody Transaction tx) {
        return service.update(id, tx);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/{id}/pay")
    public Transaction pay(@PathVariable Long id) {
        return service.processPayment(id);
    }
       @GetMapping("/user/{userId}")
    public List<Transaction> getUserTransactions(@PathVariable Long userId) {
        return service.getTransactionsByUser(userId);
    }

    @GetMapping("/order/{orderId}")
    public List<Transaction> getOrderTransactions(@PathVariable Long orderId) {
        return service.getTransactionsByOrder(orderId);
    }
}
