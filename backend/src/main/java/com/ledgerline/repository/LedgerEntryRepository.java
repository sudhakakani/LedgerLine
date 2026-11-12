package com.ledgerline.repository;
import com.ledgerline.entity.LedgerEntry; import java.util.List; import org.springframework.data.jpa.repository.JpaRepository;
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry,Long>{List<LedgerEntry> findTop100ByAccountIdOrderByCreatedAtDesc(Long accountId);}
