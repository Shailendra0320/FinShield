package com.finshield.graphql;

import com.finshield.account.model.Account;
import com.finshield.account.repository.AccountRepository;
import com.finshield.audit.model.AuditLog;
import com.finshield.audit.repository.AuditLogRepository;
import com.finshield.ledger.model.JournalTransaction;
import com.finshield.ledger.repository.JournalTransactionRepository;
import com.finshield.makerchecker.service.MakerCheckerService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import java.util.List;

@Controller
public class LedgerGraphQLController {

    private final AccountRepository accountRepository;
    private final JournalTransactionRepository transactionRepository;
    private final MakerCheckerService makerCheckerService;
    private final AuditLogRepository auditLogRepository;

    public LedgerGraphQLController(
            AccountRepository accountRepository,
            JournalTransactionRepository transactionRepository,
            MakerCheckerService makerCheckerService,
            AuditLogRepository auditLogRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.makerCheckerService = makerCheckerService;
        this.auditLogRepository = auditLogRepository;
    }

    @QueryMapping
    public Account account(@Argument Long id) {
        return accountRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<Account> allAccounts() {
        return accountRepository.findAll();
    }

    @QueryMapping
    public List<JournalTransaction> recentTransactions(@Argument Integer limit) {
        return transactionRepository.findTop20ByOrderByCreatedAtDesc();
    }

    @QueryMapping
    public List<JournalTransaction> pendingMakerCheckerQueue() {
        return makerCheckerService.getPendingQueue();
    }

    @QueryMapping
    public List<AuditLog> auditLogs(@Argument Integer limit) {
        return auditLogRepository.findTop50ByOrderByTimestampDesc();
    }
}