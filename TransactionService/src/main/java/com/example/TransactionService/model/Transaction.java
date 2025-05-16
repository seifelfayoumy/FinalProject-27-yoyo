package com.example.TransactionService.model;
import jakarta.persistence.*;
import lombok.*;
import org.antlr.v4.runtime.misc.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

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
    
    @ElementCollection
    @CollectionTable(name = "transaction_products", 
                    joinColumns = @JoinColumn(name = "transaction_id"))
    private List<String> products; // Format: "ProductID-quantity"

    @PrePersist
    void onInsert() { createdAt = Instant.now(); }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }
}
