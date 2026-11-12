package com.ledgerline.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name="accounts")
public class Account {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="owner_id",nullable=false) private AppUser owner;
    @Column(nullable=false,length=80) private String name;
    @Column(nullable=false,length=3) private String currency;
    @Column(nullable=false,precision=19,scale=2) private BigDecimal balance=BigDecimal.ZERO;
    @Column(nullable=false,length=20) private String status="ACTIVE";
    @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();
    protected Account() {}
    public Account(AppUser owner,String name,String currency){this.owner=owner;this.name=name;this.currency=currency.toUpperCase();this.balance=BigDecimal.ZERO;this.status="ACTIVE";}
    public Long getId(){return id;} public AppUser getOwner(){return owner;} public String getName(){return name;} public String getCurrency(){return currency;} public BigDecimal getBalance(){return balance;} public String getStatus(){return status;} public Instant getCreatedAt(){return createdAt;}
    public void credit(BigDecimal amount){
        requirePositive(amount);
        balance=balance.add(amount);
    }
    public void debit(BigDecimal amount){
        requirePositive(amount);
        if(balance.compareTo(amount)<0)throw new IllegalArgumentException("Insufficient funds");
        balance=balance.subtract(amount);
    }
    private void requirePositive(BigDecimal amount){
        if(amount==null || amount.compareTo(BigDecimal.ZERO)<=0)throw new IllegalArgumentException("Amount must be positive");
    }
}
