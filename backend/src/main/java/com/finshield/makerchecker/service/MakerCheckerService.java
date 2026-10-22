package com.finshield.makerchecker.service;

import com.finshield.account.model.Account;
import com.finshield.account.repository.AccountRepository;
import com.finshield.audit.service.AuditService;
import com.finshield.ledger.dto.TransferResponse;
import com.finshield.ledger.model.EntryType;
import com.finshield.ledger.model.JournalTransaction;
import com.finshield.ledger.model.LedgerEntry;
import com.finshield.ledger.model.TransactionStatus;
import com.finshield.ledger.repository.JournalTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MakerCheckerService {

    private final JournalTransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AuditService auditService;

    public MakerCheckerService(
            JournalTransactionRepository transactionRepository,
            AccountRepository accountRepository,
            AuditService auditService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.auditService = auditService;
    }

    public List<JournalTransaction> getPendingQueue() {
        return transactionRepository.findByStatus(TransactionStatus.PENDING_APPROVAL);
    }

    @Transactional
    public TransferResponse approveTransaction(Long transactionId, String reviewer, String comments) {
        JournalTransaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (tx.getStatus() != TransactionStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Transaction is not pending approval");
        }

        Account src = accountRepository.findByAccountNumberWithLock(tx.getSourceAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        Account dest = accountRepository.findByAccountNumberWithLock(tx.getDestinationAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));

        if (src.getBalance().compareTo(tx.getAmount()) < 0) {
            tx.setStatus(TransactionStatus.REJECTED);
            tx.setReviewer(reviewer);
            tx.setReviewComments("Auto-rejected upon approval attempt: Insufficient funds");
            transactionRepository.save(tx);
            throw new IllegalStateException("Insufficient balance at approval time");
        }

        src.setBalance(src.getBalance().subtract(tx.getAmount()));
        dest.setBalance(dest.getBalance().add(tx.getAmount()));
        accountRepository.save(src);
        accountRepository.save(dest);

        tx.addEntry(new LedgerEntry(src.getAccountNumber(), EntryType.DEBIT, tx.getAmount()));
        tx.addEntry(new LedgerEntry(dest.getAccountNumber(), EntryType.CREDIT, tx.getAmount()));
        tx.setStatus(TransactionStatus.APPROVED);
        tx.setReviewer(reviewer);
        tx.setReviewComments(comments);
        transactionRepository.save(tx);

        auditService.log("MAKER_CHECKER_APPROVED", reviewer, "system",
                "Transaction " + tx.getTransactionReference() + " approved by " + reviewer + ". Comments: " + comments);

        return new TransferResponse(
                true,
                tx.getTransactionReference(),
                TransactionStatus.APPROVED,
                tx.getAmount(),
                tx.getRiskScore(),
                "Transaction approved and double-entry executed by Checker " + reviewer
        );
    }

    @Transactional
    public TransferResponse rejectTransaction(Long transactionId, String reviewer, String reason) {
        JournalTransaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        tx.setStatus(TransactionStatus.REJECTED);
        tx.setReviewer(reviewer);
        tx.setReviewComments(reason);
        transactionRepository.save(tx);

        auditService.log("MAKER_CHECKER_REJECTED", reviewer, "system",
                "Transaction " + tx.getTransactionReference() + " rejected by " + reviewer + ". Reason: " + reason);

        return new TransferResponse(
                false,
                tx.getTransactionReference(),
                TransactionStatus.REJECTED,
                tx.getAmount(),
                tx.getRiskScore(),
                "Transaction rejected by Checker: " + reason
        );
    }
}