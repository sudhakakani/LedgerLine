package com.ledgerline.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="users")
public class AppUser {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,length=120) private String fullName;
    @Column(nullable=false,unique=true,length=180) private String email;
    @Column(nullable=false) private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private Role role=Role.ACCOUNT_HOLDER;
    @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();
    protected AppUser() {}
    public AppUser(String fullName,String email,String passwordHash){this.fullName=fullName;this.email=email.toLowerCase();this.passwordHash=passwordHash;this.role=Role.ACCOUNT_HOLDER;}
    public Long getId(){return id;} public String getFullName(){return fullName;} public String getEmail(){return email;} public String getPasswordHash(){return passwordHash;} public Role getRole(){return role;} public Instant getCreatedAt(){return createdAt;}
}
