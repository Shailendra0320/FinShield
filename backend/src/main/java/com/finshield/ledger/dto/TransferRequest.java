package com.finshield.ledger.dto;

import java.math.BigDecimal;

public record TransferRequest(
        String sourceAccountNumber,
        String destinationAccountNumber,
        BigDecimal amount,
        String description,
        String idempotencyKey
) {}