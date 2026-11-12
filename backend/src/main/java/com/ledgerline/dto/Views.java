package com.ledgerline.dto;
import com.ledgerline.entity.*; import java.math.BigDecimal; import java.time.Instant;
public final class Views {private Views(){}
 public record UserView(Long id,String fullName,String email,String role,Instant createdAt){public static UserView of(AppUser u){return new UserView(u.getId(),u.getFullName(),u.getEmail(),u.getRole().name(),u.getCreatedAt());}}
 public record AccountView(Long id,String name,String currency,BigDecimal balance,String status,Instant createdAt){public static AccountView of(Account a){return new AccountView(a.getId(),a.getName(),a.getCurrency(),a.getBalance(),a.getStatus(),a.getCreatedAt());}}
 public record EntryView(Long id,String transactionRef,Long accountId,String entryType,BigDecimal amount,BigDecimal balanceAfter,String description,Instant createdAt){public static EntryView of(LedgerEntry e){return new EntryView(e.getId(),e.getTransactionRef(),e.getAccountId(),e.getEntryType(),e.getAmount(),e.getBalanceAfter(),e.getDescription(),e.getCreatedAt());}}
 public record TransactionView(String transactionRef,String status,String message){}
}
