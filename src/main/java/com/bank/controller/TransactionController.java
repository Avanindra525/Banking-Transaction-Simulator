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

import com.bank.dto.TransactionRequest;
import com.bank.model.Transaction;
import com.bank.security.AccessValidator;
import com.bank.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccessValidator accessValidator;

    @PostMapping("/process")
    public ResponseEntity<Transaction> process(@RequestBody TransactionRequest request, @RequestParam Long userId) {
        accessValidator.validateUserAccess(userId);
        Transaction transaction = transactionService.processTransaction(request, userId);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/user/{userId}")
    public List<Transaction> getUserTransactions(@PathVariable Long userId) {
        accessValidator.validateUserAccess(userId);
        return transactionService.getUserTransactions(userId);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Transaction>> getAccountTransactions(@PathVariable Long accountId, @RequestParam Long userId) {
        accessValidator.validateUserAccess(userId);
        return ResponseEntity.ok(transactionService.getAccountTransactionsForUser(accountId, userId));
    }
}

