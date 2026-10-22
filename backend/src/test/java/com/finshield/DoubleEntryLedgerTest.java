package com.finshield;

import com.finshield.account.model.Account;
import com.finshield.account.model.AccountStatus;
import com.finshield.account.model.AccountType;
import com.finshield.account.repository.AccountRepository;
import com.finshield.ledger.dto.TransferRequest;
import com.finshield.ledger.dto.TransferResponse;
import com.finshield.ledger.model.TransactionStatus;
import com.finshield.ledger.service.DoubleEntryLedgerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DoubleEntryLedgerTest {

    @Autowired
    private DoubleEntryLedgerService ledgerService;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("Verify Double-Entry Invariant: Debit reduces source balance and credit increases destination balance by exact amount")
    void testAtomicDoubleEntryTransfer() {
        Account src = accountRepository.findByAccountNumber("ACC-2001").orElseThrow();
        Account dest = accountRepository.findByAccountNumber("ACC-2002").orElseThrow();

        BigDecimal srcBefore = src.getBalance();
        BigDecimal destBefore = dest.getBalance();
        BigDecimal transferAmount = new BigDecimal("25000.00");

        TransferRequest request = new TransferRequest(
                src.getAccountNumber(),
                dest.getAccountNumber(),
                transferAmount,
                "Vendor Settlement",
                "test-idemp-" + System.currentTimeMillis()
        );

        TransferResponse response = ledgerService.processTransfer(request, "test_runner", "127.0.0.1");

        assertTrue(response.success());
        assertEquals(TransactionStatus.EXECUTED, response.status());

        Account srcAfter = accountRepository.findByAccountNumber("ACC-2001").orElseThrow();
        Account destAfter = accountRepository.findByAccountNumber("ACC-2002").orElseThrow();

        // Mathematical invariant: Total delta across the ledger is ZERO
        assertEquals(srcBefore.subtract(transferAmount), srcAfter.getBalance());
        assertEquals(destBefore.add(transferAmount), destAfter.getBalance());
    }

    @Test
    @DisplayName("Maker-Checker Guard: Transactions exceeding INR 100,000 must pause with status PENDING_APPROVAL")
    void testMakerCheckerThresholdTrigger() {
        TransferRequest highValueRequest = new TransferRequest(
                "ACC-2001",
                "ACC-2002",
                new BigDecimal("150000.00"),
                "High Value Commercial Transfer",
                "high-val-idemp-" + System.currentTimeMillis()
        );

        TransferResponse response = ledgerService.processTransfer(highValueRequest, "maker_user", "127.0.0.1");

        assertTrue(response.success());
        assertEquals(TransactionStatus.PENDING_APPROVAL, response.status());
    }
}