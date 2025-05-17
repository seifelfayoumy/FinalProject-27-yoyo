package com.example.TransactionService.model;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.antlr.v4.runtime.misc.NotNull;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long orderId;
    @NotNull private Long userId;

    private BigDecimal amount;

    private String currency;                 // ISO‑4217

    private String status;                   // NEW, PAID, REFUNDED …

    private Instant createdAt;
    private Instant updatedAt;
    private String paymentMethod;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "transaction_products", 
                    joinColumns = @JoinColumn(name = "transaction_id"))
    private List<String> products; // Format: "ProductID-quantity"

    @PrePersist
    void onInsert() { createdAt = Instant.now(); }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }
}
