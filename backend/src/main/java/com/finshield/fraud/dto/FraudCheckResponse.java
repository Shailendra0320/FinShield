package com.finshield.fraud.dto;

public record FraudCheckResponse(
        double anomalyScore,
        boolean isFlagged,
        String riskLevel,
        String explanation
) {}