package com.example.TransactionService.controller;

import com.example.TransactionService.model.Transaction;
import com.example.TransactionService.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction create(@RequestBody Transaction tx) {
        return service.create(tx);
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
}
