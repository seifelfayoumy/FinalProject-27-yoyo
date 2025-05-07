package com.example.TransactionService.service;

import com.example.TransactionService.model.Transaction;
import com.example.TransactionService.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository repo;

    public Transaction create(Transaction tx)               { return repo.save(tx); }
    public Transaction get(Long id)                         { return repo.findById(id).orElseThrow(); }
    public List<Transaction> list()                         { return repo.findAll(); }
    public Transaction update(Long id, Transaction tx)      { tx.setId(id); return repo.save(tx); }
    public void delete(Long id)                             { repo.deleteById(id); }
}

