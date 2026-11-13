package com.ledgerline.service;

import com.ledgerline.dto.Requests;
import com.ledgerline.dto.Views;
import com.ledgerline.entity.Account;
import com.ledgerline.entity.AuditEvent;
import com.ledgerline.entity.IdempotencyRecord;
import com.ledgerline.entity.LedgerEntry;
import com.ledgerline.repository.AccountRepository;
import com.ledgerline.repository.AuditEventRepository;
import com.ledgerline.repository.IdempotencyRepository;
import com.ledgerline.repository.LedgerEntryRepository;
import com.ledgerline.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerService {
    private final AccountRepository accounts;
    private final LedgerEntryRepository entries;
    private final IdempotencyRepository keys;
    private final UserRepository users;
    private final AuditEventRepository audits;

    public LedgerService(AccountRepository accounts, LedgerEntryRepository entries,
            IdempotencyRepository keys, UserRepository users, AuditEventRepository audits) {
        this.accounts = accounts;
        this.entries = entries;
        this.keys = keys;
        this.users = users;
        this.audits = audits;
    }

    private String ref() { return UUID.randomUUID().toString(); }

    private BigDecimal amount(BigDecimal value) {
        if (value == null || value.scale() > 2 || value.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be positive and have at most 2 decimal places");
        return value.setScale(2, RoundingMode.UNNECESSARY);
    }

    private Long ownerId(String email) {
        return users.findByEmail(email).orElseThrow(() -> new NoSuchElementException("User not found")).getId();
    }

    private void own(Account account, String email) {
        if (!account.getOwner().getEmail().equalsIgnoreCase(email))
            throw new NoSuchElementException("Account not found");
    }

    private String requestHash(String operation, String email, String details) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((operation + "|" + email.toLowerCase() + "|" + details).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    private Optional<Views.TransactionView> prior(String key, String email, String hash) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Idempotency-Key header is required");
        if (key.length() > 100) throw new IllegalArgumentException("Idempotency-Key must be 100 characters or fewer");
        return keys.findById(key).map(record -> {
            if (record.getOwnerEmail() == null || record.getRequestHash() == null
                    || !record.getOwnerEmail().equalsIgnoreCase(email)
                    || !record.getRequestHash().equals(hash)) {
                throw new IllegalArgumentException("Idempotency-Key was already used for a different request");
            }
            return new Views.TransactionView(record.getTransactionRef(), "ALREADY_PROCESSED",
                    "Duplicate request ignored; original transaction was retained");
        });
    }

    private void remember(String key, String email, String hash, String ref) {
        keys.save(new IdempotencyRecord(key, email.toLowerCase(), hash, ref));
    }

    @Transactional
    public Views.TransactionView deposit(String email, Long id, Requests.Money req, String key) {
        BigDecimal value = amount(req.amount());
        String hash = requestHash("DEPOSIT", email, id + "|" + value + "|" + req.description());
        var old = prior(key, email, hash);
        if (old.isPresent()) return old.get();
        Account account = accounts.findLockedByIdAndOwnerId(id, ownerId(email))
                .orElseThrow(() -> new NoSuchElementException("Account not found"));
        own(account, email);
        account.credit(value);
        String reference = ref();
        entries.save(new LedgerEntry(reference, account, "CREDIT", value, account.getBalance(),
                req.description() == null ? "Deposit" : req.description()));
        remember(key, email, hash, reference);
        audits.save(new AuditEvent(email, "DEPOSIT", reference, "Account " + id + " credited " + value));
        return new Views.TransactionView(reference, "COMPLETED", "Deposit completed");
    }

    @Transactional
    public Views.TransactionView withdraw(String email, Long id, Requests.Money req, String key) {
        BigDecimal value = amount(req.amount());
        String hash = requestHash("WITHDRAWAL", email, id + "|" + value + "|" + req.description());
        var old = prior(key, email, hash);
        if (old.isPresent()) return old.get();
        Account account = accounts.findLockedByIdAndOwnerId(id, ownerId(email))
                .orElseThrow(() -> new NoSuchElementException("Account not found"));
        own(account, email);
        account.debit(value);
        String reference = ref();
        entries.save(new LedgerEntry(reference, account, "DEBIT", value, account.getBalance(),
                req.description() == null ? "Withdrawal" : req.description()));
        remember(key, email, hash, reference);
        audits.save(new AuditEvent(email, "WITHDRAWAL", reference, "Account " + id + " debited " + value));
        return new Views.TransactionView(reference, "COMPLETED", "Withdrawal completed");
    }

    @Transactional
    public Views.TransactionView transfer(String email, Requests.Transfer req, String key) {
        if (req.fromAccountId().equals(req.toAccountId()))
            throw new IllegalArgumentException("Source and destination accounts must differ");
        BigDecimal value = amount(req.amount());
        String hash = requestHash("TRANSFER", email, req.fromAccountId() + "|" + req.toAccountId()
                + "|" + value + "|" + req.description());
        var old = prior(key, email, hash);
        if (old.isPresent()) return old.get();
        Long first = Math.min(req.fromAccountId(), req.toAccountId());
        Long second = Math.max(req.fromAccountId(), req.toAccountId());
        Long owner = ownerId(email);
        Account one = accounts.findLockedByIdAndOwnerId(first, owner)
                .orElseThrow(() -> new NoSuchElementException("Account not found"));
        Account two = accounts.findLockedByIdAndOwnerId(second, owner)
                .orElseThrow(() -> new NoSuchElementException("Account not found"));
        Account from = req.fromAccountId().equals(first) ? one : two;
        Account to = req.toAccountId().equals(first) ? one : two;
        own(from, email);
        own(to, email);
        if (!from.getCurrency().equals(to.getCurrency()))
            throw new IllegalArgumentException("Transfers require matching account currencies");
        from.debit(value);
        to.credit(value);
        String reference = ref();
        String description = req.description() == null ? "Account transfer" : req.description();
        entries.save(new LedgerEntry(reference, from, "DEBIT", value, from.getBalance(), description + " (outgoing)"));
        entries.save(new LedgerEntry(reference, to, "CREDIT", value, to.getBalance(), description + " (incoming)"));
        remember(key, email, hash, reference);
        audits.save(new AuditEvent(email, "TRANSFER", reference,
                "Transfer from account " + from.getId() + " to account " + to.getId() + " amount " + value));
        return new Views.TransactionView(reference, "COMPLETED", "Transfer completed");
    }

    @Transactional(readOnly = true)
    public List<Views.EntryView> history(String email, Long id) {
        Account account = accounts.findById(id).orElseThrow(() -> new NoSuchElementException("Account not found"));
        own(account, email);
        return entries.findTop100ByAccount_IdOrderByCreatedAtDesc(id).stream().map(Views.EntryView::of).toList();
    }
}
