package com.finshield;

import com.finshield.account.model.Account;
import com.finshield.account.model.AccountStatus;
import com.finshield.account.model.AccountType;
import com.finshield.account.repository.AccountRepository;
import com.finshield.auth.model.Role;
import com.finshield.auth.model.User;
import com.finshield.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

/**
 * FinShield Modular Monolith
 * Built by Shailendra Vishwakarma
 * 
 * Enterprise Double-Entry Banking Ledger, Apache Fineract Chart of Accounts (COA),
 * Maker-Checker Dual Control, and AI Fraud Intelligence.
 */
@SpringBootApplication
public class FinShieldApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinShieldApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                // Seed Admin
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFullName("Shailendra Vishwakarma");
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);

                // Seed Compliance Officer (Checker)
                User checker = new User();
                checker.setUsername("compliance");
                checker.setPassword(passwordEncoder.encode("compliance123"));
                checker.setFullName("Compliance Officer");
                checker.setRole(Role.COMPLIANCE_OFFICER);
                userRepository.save(checker);

                // Seed Customer (Maker)
                User customer = new User();
                customer.setUsername("john_doe");
                customer.setPassword(passwordEncoder.encode("password123"));
                customer.setFullName("John Doe");
                customer.setRole(Role.CUSTOMER);
                userRepository.save(customer);
            }

            if (accountRepository.count() == 0) {
                // 1. Bank Reserve Asset Account (Central Liquidity Vault)
                Account vault = new Account();
                vault.setAccountNumber("VAULT-1001");
                vault.setName("Central Reserve Vault (Asset)");
                vault.setType(AccountType.ASSET);
                vault.setStatus(AccountStatus.ACTIVE);
                vault.setBalance(new BigDecimal("10000000.00")); // 10 Million INR Reserve
                vault.setCurrency("INR");
                accountRepository.save(vault);

                // 2. Customer Deposit Account 1 (Liability)
                Account userAcc1 = new Account();
                userAcc1.setAccountNumber("ACC-2001");
                userAcc1.setName("John Doe Savings Account");
                userAcc1.setType(AccountType.LIABILITY);
                userAcc1.setStatus(AccountStatus.ACTIVE);
                userAcc1.setBalance(new BigDecimal("250000.00")); // 2.5 Lakhs
                userAcc1.setCurrency("INR");
                accountRepository.save(userAcc1);

                // 3. Customer Deposit Account 2 (Liability)
                Account userAcc2 = new Account();
                userAcc2.setAccountNumber("ACC-2002");
                userAcc2.setName("Alice Smith Checking Account");
                userAcc2.setType(AccountType.LIABILITY);
                userAcc2.setStatus(AccountStatus.ACTIVE);
                userAcc2.setBalance(new BigDecimal("150000.00")); // 1.5 Lakhs
                userAcc2.setCurrency("INR");
                accountRepository.save(userAcc2);

                // 4. Platform Fee Account (Income)
                Account feeAcc = new Account();
                feeAcc.setAccountNumber("FEE-3001");
                feeAcc.setName("Transaction Processing Revenue (Income)");
                feeAcc.setType(AccountType.INCOME);
                feeAcc.setStatus(AccountStatus.ACTIVE);
                feeAcc.setBalance(BigDecimal.ZERO);
                feeAcc.setCurrency("INR");
                accountRepository.save(feeAcc);
            }
        };
    }
}