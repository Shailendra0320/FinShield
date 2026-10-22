package com.finshield.account.controller;

import com.finshield.account.dto.CreateAccountRequest;
import com.finshield.account.model.Account;
import com.finshield.account.service.AccountService;
import com.finshield.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Account>>> listAccounts() {
        return ResponseEntity.ok(ApiResponse.ok("Accounts retrieved", accountService.getAllAccounts()));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<Account>> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(ApiResponse.ok("Account details", accountService.getAccountByNumber(accountNumber)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Account>> createAccount(@RequestBody CreateAccountRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Account created successfully", accountService.createAccount(request)));
    }
}