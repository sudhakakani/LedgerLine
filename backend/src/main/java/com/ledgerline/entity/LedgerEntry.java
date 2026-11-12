package com.ledgerline.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="ledger_entries", indexes={@Index(name="idx_ledger_account_created",columnList="account_id,created_at")})
public class LedgerEntry {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,length=36) private String transactionRef;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="account_id",nullable=false) private Account account;
    @Column(nullable=false,length=10) private String entryType;
    @Column(nullable=false,precision=19,scale=2) private BigDecimal amount;
    @Column(nullable=false,precision=19,scale=2) private BigDecimal balanceAfter;
    @Column(length=240) private String description;
    @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();
    protected LedgerEntry() {}
    public LedgerEntry(String ref,Account account,String type,BigDecimal amount,BigDecimal balanceAfter,String description){this.transactionRef=ref;this.account=account;this.entryType=type;this.amount=amount;this.balanceAfter=balanceAfter;this.description=description;}
    public Long getId(){return id;} public String getTransactionRef(){return transactionRef;} public Long getAccountId(){return account.getId();} public String getEntryType(){return entryType;} public BigDecimal getAmount(){return amount;} public BigDecimal getBalanceAfter(){return balanceAfter;} public String getDescription(){return description;} public Instant getCreatedAt(){return createdAt;}
}
