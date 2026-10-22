package com.finshield.ledger.service;

import com.finshield.account.model.Account;
import com.finshield.account.model.AccountStatus;
import com.finshield.account.repository.AccountRepository;
import com.finshield.audit.service.AuditService;
import com.finshield.fraud.client.FraudDetectionClient;
import com.finshield.fraud.dto.FraudCheckRequest;
import com.finshield.fraud.dto.FraudCheckResponse;
import com.finshield.ledger.dto.TransferRequest;
import com.finshield.ledger.dto.TransferResponse;
import com.finshield.ledger.model.EntryType;
import com.finshield.ledger.model.JournalTransaction;
import com.finshield.ledger.model.LedgerEntry;
import com.finshield.ledger.model.TransactionStatus;
import com.finshield.ledger.repository.JournalTransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class DoubleEntryLedgerService {

    private final AccountRepository accountRepository;
    private final JournalTransactionRepository transactionRepository;
    private final FraudDetectionClient fraudClient;
    private final AuditService auditService;
    private final BigDecimal makerCheckerThreshold;

    public DoubleEntryLedgerService(
            AccountRepository accountRepository,
            JournalTransactionRepository transactionRepository,
            FraudDetectionClient fraudClient,
            AuditService auditService,
            @Value("${finshield.maker-checker.threshold:100000.00}") BigDecimal makerCheckerThreshold) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.fraudClient = fraudClient;
        this.auditService = auditService;
        this.makerCheckerThreshold = makerCheckerThreshold;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransferResponse processTransfer(TransferRequest request, String initiator, String clientIp) {
        if (request.idempotencyKey() != null) {
            Optional<JournalTransaction> existing = transactionRepository.findByIdempotencyKey(request.idempotencyKey());
            if (existing.isPresent()) {
                JournalTransaction tx = existing.get();
                return new TransferResponse(
                        true,
                        tx.getTransactionReference(),
                        tx.getStatus(),
                        tx.getAmount(),
                        tx.getRiskScore(),
                        "Idempotent response: transaction previously processed."
                );
            }
        }

        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be strictly positive");
        }

        if (request.sourceAccountNumber().equals(request.destinationAccountNumber())) {
            throw new IllegalArgumentException("Source and destination accounts must be distinct");
        }

        String ref = "TX-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        FraudCheckResponse fraudEval = fraudClient.evaluateRisk(new FraudCheckRequest(
                ref,
                request.sourceAccountNumber(),
                request.destinationAccountNumber(),
                request.amount(),
                clientIp
        ));

        boolean highValue = request.amount().compareTo(makerCheckerThreshold) >= 0;
        boolean requiresApproval = highValue || fraudEval.isFlagged();

        JournalTransaction tx = new JournalTransaction();
        tx.setTransactionReference(ref);
        tx.setIdempotencyKey(request.idempotencyKey());
        tx.setDescription(request.description() != null ? request.description() : "Funds Transfer");
        tx.setAmount(request.amount());
        tx.setRiskScore(fraudEval.anomalyScore());
        tx.setRequiresApproval(requiresApproval);
        tx.setSourceAccountNumber(request.sourceAccountNumber());
        tx.setDestinationAccountNumber(request.destinationAccountNumber());
        tx.setInitiator(initiator != null ? initiator : "john_doe");

        if (requiresApproval) {
            tx.setStatus(TransactionStatus.PENDING_APPROVAL);
            transactionRepository.save(tx);
            auditService.log("TRANSFER_HELD_FOR_REVIEW", initiator, clientIp,
                    "Transfer " + ref + " held for Maker-Checker review. Reason: " + fraudEval.explanation());
            return new TransferResponse(
                    true,
                    ref,
                    TransactionStatus.PENDING_APPROVAL,
                    request.amount(),
                    fraudEval.anomalyScore(),
                    "Transaction requires Maker-Checker approval: " + fraudEval.explanation()
            );
        }

        String firstLock = request.sourceAccountNumber().compareTo(request.destinationAccountNumber()) < 0
                ? request.sourceAccountNumber() : request.destinationAccountNumber();
        String secondLock = firstLock.equals(request.sourceAccountNumber())
                ? request.destinationAccountNumber() : request.sourceAccountNumber();

        accountRepository.findByAccountNumberWithLock(firstLock)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + firstLock));
        accountRepository.findByAccountNumberWithLock(secondLock)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + secondLock));

        Account src = accountRepository.findByAccountNumber(request.sourceAccountNumber()).get();
        Account dest = accountRepository.findByAccountNumber(request.destinationAccountNumber()).get();

        if (src.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Source account is not active");
        }
        if (dest.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Destination account is not active");
        }

        if (src.getBalance().compareTo(request.amount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds in source account " + src.getAccountNumber());
        }

        src.setBalance(src.getBalance().subtract(request.amount()));
        dest.setBalance(dest.getBalance().add(request.amount()));
        accountRepository.save(src);
        accountRepository.save(dest);

        LedgerEntry debitEntry = new LedgerEntry(src.getAccountNumber(), EntryType.DEBIT, request.amount());
        LedgerEntry creditEntry = new LedgerEntry(dest.getAccountNumber(), EntryType.CREDIT, request.amount());

        tx.addEntry(debitEntry);
        tx.addEntry(creditEntry);
        tx.setStatus(TransactionStatus.EXECUTED);
        transactionRepository.save(tx);

        auditService.log("TRANSFER_EXECUTED", initiator, clientIp,
                "Transferred INR " + request.amount() + " from " + src.getAccountNumber() + " to " + dest.getAccountNumber());

        return new TransferResponse(
                true,
                ref,
                TransactionStatus.EXECUTED,
                request.amount(),
                fraudEval.anomalyScore(),
                "Transfer executed successfully under Double-Entry Ledger rules."
        );
    }
}