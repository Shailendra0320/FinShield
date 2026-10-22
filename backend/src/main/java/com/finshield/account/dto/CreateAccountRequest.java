package com.finshield.account.dto;

import com.finshield.account.model.AccountType;
import java.math.BigDecimal;

public record CreateAccountRequest(
        String name,
        AccountType type,
        BigDecimal initialBalance,
        String currency
) {}