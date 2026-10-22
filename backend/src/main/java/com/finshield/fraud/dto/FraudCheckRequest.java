package com.finshield.fraud.dto;

import java.math.BigDecimal;

public record FraudCheckRequest(
        String transactionReference,
        String sourceAccountNumber,
        String destinationAccountNumber,
        BigDecimal amount,
        String clientIp
) {}