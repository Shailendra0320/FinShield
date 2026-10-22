package com.finshield.ledger.controller;

import com.finshield.common.response.ApiResponse;
import com.finshield.ledger.dto.TransferRequest;
import com.finshield.ledger.dto.TransferResponse;
import com.finshield.ledger.model.JournalTransaction;
import com.finshield.ledger.repository.JournalTransactionRepository;
import com.finshield.ledger.service.DoubleEntryLedgerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ledger")
public class LedgerController {

    private final DoubleEntryLedgerService ledgerService;
    private final JournalTransactionRepository transactionRepository;

    public LedgerController(
            DoubleEntryLedgerService ledgerService,
            JournalTransactionRepository transactionRepository) {
        this.ledgerService = ledgerService;
        this.transactionRepository = transactionRepository;
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransferResponse>> transfer(
            @RequestBody TransferRequest request,
            HttpServletRequest httpServletRequest) {
        String clientIp = httpServletRequest.getRemoteAddr();
        TransferResponse response = ledgerService.processTransfer(request, "customer_api", clientIp);
        return ResponseEntity.ok(ApiResponse.ok("Transfer processed", response));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<JournalTransaction>>> getRecentTransactions() {
        return ResponseEntity.ok(ApiResponse.ok("Recent transactions", transactionRepository.findTop20ByOrderByCreatedAtDesc()));
    }
}