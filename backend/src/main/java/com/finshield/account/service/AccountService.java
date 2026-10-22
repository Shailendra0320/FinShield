package com.finshield.account.service;

import com.finshield.account.dto.CreateAccountRequest;
import com.finshield.account.model.Account;
import com.finshield.account.model.AccountStatus;
import com.finshield.account.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountNumber));
    }

    @Transactional
    public Account createAccount(CreateAccountRequest request) {
        Account account = new Account();
        account.setAccountNumber("ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        account.setName(request.name());
        account.setType(request.type());
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(request.initialBalance() != null ? request.initialBalance() : BigDecimal.ZERO);
        account.setCurrency(request.currency() != null ? request.currency() : "INR");
        return accountRepository.save(account);
    }
}