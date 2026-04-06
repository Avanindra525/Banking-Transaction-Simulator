package com.bank.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bank.dto.AlertDto;
import com.bank.model.Account;
import com.bank.model.Alert;
import com.bank.model.Transaction;
import com.bank.repository.AccountRepository;
import com.bank.repository.AlertRepository;

@Service
public class AlertService {

    private final double lowBalanceThreshold;
    private final double largeTransactionThreshold;

    private final AccountRepository accountRepository;
    private final AlertRepository alertRepository;
    private final EmailService emailService;

    public AlertService(
            AccountRepository accountRepository,
            AlertRepository alertRepository,
            EmailService emailService,
            @Value("${bank.alert.low-balance-threshold:1000}") double lowBalanceThreshold,
            @Value("${bank.alert.large-transaction-threshold:10000}") double largeTransactionThreshold) {
        this.accountRepository = accountRepository;
        this.alertRepository = alertRepository;
        this.emailService = emailService;
        this.lowBalanceThreshold = lowBalanceThreshold;
        this.largeTransactionThreshold = largeTransactionThreshold;
    }

    public List<AlertDto> getAlertsForAccount(Long accountId, Long userId) {
        accountRepository.findByIdAndUser_Id(accountId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        return alertRepository.findByAccount_IdAndAccount_User_IdOrderByCreatedAtDesc(accountId, userId).stream()
                .map(this::toDto)
                .toList();
    }

    public List<AlertDto> getAlertsForUser(Long userId) {
        return alertRepository.findByAccount_User_IdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    public void triggerLowBalanceAlertIfNeeded(Account account) {
        if (account.getBalance() >= lowBalanceThreshold) {
            return;
        }

        String message = "Balance is below threshold. Current balance: Rs. "
                + String.format("%.2f", account.getBalance());
        alertRepository.save(new Alert(account, Alert.Type.LOW_BALANCE, "Low Balance", message, "HIGH"));
        emailService.sendAlertEmail(account.getEmail(), "LOW_BALANCE", message);
    }

    public void triggerLargeTransactionAlertIfNeeded(Account account, Transaction transaction) {
        if (transaction.getAmount() < largeTransactionThreshold || transaction.getStatus() == Transaction.Status.FAILED) {
            return;
        }

        String message = "Large transaction detected: " + transaction.getType() + " of Rs. "
                + String.format("%.2f", transaction.getAmount());
        alertRepository.save(new Alert(account, Alert.Type.LARGE_TRANSACTION, "Large Transaction", message, "MEDIUM"));
        emailService.sendAlertEmail(account.getEmail(), "LARGE_TRANSACTION", message);
    }

    private AlertDto toDto(Alert alert) {
        return new AlertDto(
                alert.getAccount().getId(),
                alert.getTitle(),
                alert.getMessage(),
                alert.getSeverity(),
                alert.getCreatedAt());
    }
}
