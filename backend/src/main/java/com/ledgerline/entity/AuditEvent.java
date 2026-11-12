package com.ledgerline.entity;
import jakarta.persistence.*;import java.time.Instant;
@Entity @Table(name="audit_events",indexes=@Index(name="idx_audit_created",columnList="created_at"))
public class AuditEvent {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=180) private String actorEmail;
 @Column(nullable=false,length=40) private String action;
 @Column(nullable=false,length=36) private String transactionRef;
 @Column(length=300) private String details;
 @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();
 protected AuditEvent(){} public AuditEvent(String actor,String action,String ref,String details){this.actorEmail=actor;this.action=action;this.transactionRef=ref;this.details=details;}
 public Long getId(){return id;}public String getActorEmail(){return actorEmail;}public String getAction(){return action;}public String getTransactionRef(){return transactionRef;}public String getDetails(){return details;}public Instant getCreatedAt(){return createdAt;}
}
