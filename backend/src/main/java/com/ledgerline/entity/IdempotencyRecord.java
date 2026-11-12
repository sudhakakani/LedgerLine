package com.ledgerline.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "idempotency_records")
public class IdempotencyRecord {
    @Id
    @Column(length = 100)
    private String idempotencyKey;

    // Nullable only to allow Hibernate to upgrade databases created by the first MVP.
    @Column(length = 180)
    private String ownerEmail;

    @Column(length = 64)
    private String requestHash;

    @Column(nullable = false, length = 36)
    private String transactionRef;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected IdempotencyRecord() {}

    public IdempotencyRecord(String key, String ownerEmail, String requestHash, String ref) {
        this.idempotencyKey = key;
        this.ownerEmail = ownerEmail;
        this.requestHash = requestHash;
        this.transactionRef = ref;
    }

    public String getOwnerEmail() { return ownerEmail; }
    public String getRequestHash() { return requestHash; }
    public String getTransactionRef() { return transactionRef; }
}
