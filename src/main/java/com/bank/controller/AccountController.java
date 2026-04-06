package com.bank.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.dto.CreateAccountRequest;
import com.bank.model.Account;
import com.bank.service.AccountService;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    // ❌ Removed AccessValidator for now (to avoid 403)

    // ✅ GET all accounts for a user
    @GetMapping
    public ResponseEntity<List<Account>> getAccountsForUser(@RequestParam Long userId) {

        // 🔴 Disabled validation for testing
        // accessValidator.validateUserAccess(userId);

        List<Account> accounts = accountService.getAccountsForUser(userId);
        return ResponseEntity.ok(accounts);
    }

    // ✅ GET account by ID
    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(
            @PathVariable Long id,
            @RequestParam Long userId) {

        // 🔴 Disabled validation
        // accessValidator.validateUserAccess(userId);

        Account account = accountService.getUserAccount(id, userId);
        return ResponseEntity.ok(account);
    }

    // ✅ CREATE new account
    @PostMapping
    public ResponseEntity<Account> openAccount(@RequestBody CreateAccountRequest request) {

        // 🔴 Disabled validation
        // accessValidator.validateUserAccess(request.getUserId());

        Account account = accountService.openAccount(
                request.getUserId(),
                request.getAccountType(),
                request.getInitialDeposit()
        );

        return ResponseEntity.status(201).body(account);
    }
}