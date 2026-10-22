package com.finshield.ledger.dto;

import com.finshield.ledger.model.TransactionStatus;
import java.math.BigDecimal;

public record TransferResponse(
        boolean success,
        String transactionReference,
        TransactionStatus status,
        BigDecimal amount,
        double riskScore,
        String message
) {}