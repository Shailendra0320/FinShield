package com.finshield.ledger.repository;

import com.finshield.ledger.model.JournalTransaction;
import com.finshield.ledger.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JournalTransactionRepository extends JpaRepository<JournalTransaction, Long> {
    Optional<JournalTransaction> findByIdempotencyKey(String idempotencyKey);
    Optional<JournalTransaction> findByTransactionReference(String transactionReference);
    List<JournalTransaction> findByStatus(TransactionStatus status);
    List<JournalTransaction> findTop20ByOrderByCreatedAtDesc();
}