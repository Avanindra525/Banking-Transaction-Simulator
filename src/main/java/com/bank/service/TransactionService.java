package com.bank.service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.model.Transaction.Status;
import com.bank.model.Transaction.Type;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;

@Service
@Transactional
public class TransactionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AlertService alertService;

public Transaction processTransaction(com.bank.dto.TransactionRequest request, Long userId) {
        if (request == null || request.getAccountId() == null) {
            throw new IllegalArgumentException("Account is required");
        }
        if (Double.isNaN(request.getAmount()) || Double.isInfinite(request.getAmount()) || request.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (request.getType() == null || request.getType().isBlank()) {
            throw new IllegalArgumentException("Transaction type is required");
        }

        Long accountId = Objects.requireNonNull(request.getAccountId(), "Account is required");
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // Verify user owns account
        if (!account.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized account access");
        }

        Type type;
        try {
            type = Type.valueOf(request.getType().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid transaction type");
        }

        // Balance validation
        if ((type == Type.WITHDRAW || type == Type.TRANSFER_OUT) && account.getBalance() < request.getAmount()) {
            LOGGER.warn("Transaction failed due to insufficient balance. accountId={}, userId={}, amount={}, type={}",
                    account.getId(), userId, request.getAmount(), type);
            saveFailedTransaction(account, type, request.getAmount(), request.getDescription(), "Insufficient balance");
            throw new IllegalArgumentException("Insufficient balance");
        }

        // Update balance
        double newBalance = account.getBalance();
        switch (type) {
            case DEPOSIT -> newBalance += request.getAmount();
            case WITHDRAW -> newBalance -= request.getAmount();
            case TRANSFER_OUT -> {
                if (request.getToAccountId() == null) {
                    LOGGER.warn("Transaction failed due to missing target account. accountId={}, userId={}", account.getId(), userId);
                    saveFailedTransaction(account, type, request.getAmount(), request.getDescription(), "Target account is required for transfer");
                    throw new IllegalArgumentException("Target account is required for transfer");
                }
                if (request.getToAccountId().equals(account.getId())) {
                    LOGGER.warn("Transaction failed due to same source/target account. accountId={}, userId={}", account.getId(), userId);
                    saveFailedTransaction(account, type, request.getAmount(), request.getDescription(), "Cannot transfer to the same account");
                    throw new IllegalArgumentException("Cannot transfer to the same account");
                }
                newBalance -= request.getAmount();
                // Handle transfer to other account
                Long toAccountId = Objects.requireNonNull(request.getToAccountId(), "Target account is required for transfer");
                Account toAccount = accountRepository.findById(toAccountId).orElse(null);
                if (toAccount == null) {
                    saveFailedTransaction(account, type, request.getAmount(), request.getDescription(), "Target account not found");
                    throw new IllegalArgumentException("Target account not found");
                }
                if (!toAccount.getUserId().equals(userId)) {
                    saveFailedTransaction(account, type, request.getAmount(), request.getDescription(), "Target account is not owned by user");
                    throw new IllegalArgumentException("Target account is not owned by user");
                }

                toAccount.setBalance(toAccount.getBalance() + request.getAmount());
                accountRepository.save(toAccount);
                Transaction transferIn = new Transaction(toAccount, Type.TRANSFER_IN, request.getAmount(), "Transfer from " + account.getName());
                transferIn.setStatus(Status.SUCCESS);
                Transaction savedTransferIn = transactionRepository.save(transferIn);
                alertService.triggerLargeTransactionAlertIfNeeded(toAccount, savedTransferIn);
                alertService.triggerLowBalanceAlertIfNeeded(toAccount);
            }
            case TRANSFER_IN -> throw new IllegalArgumentException("TRANSFER_IN is system-generated");
        }
        account.setBalance(newBalance);
        accountRepository.save(account);

        // Create transaction record
        Transaction transaction = new Transaction(account, type, request.getAmount(), request.getDescription());
        transaction.setStatus(Status.SUCCESS);
        Transaction saved = transactionRepository.save(transaction);

        LOGGER.info("Transaction successful. accountId={}, userId={}, type={}, amount={}, transactionId={}",
            account.getId(), userId, type, request.getAmount(), saved.getId());

        alertService.triggerLargeTransactionAlertIfNeeded(account, saved);
        alertService.triggerLowBalanceAlertIfNeeded(account);

        return saved;
    }

    public List<Transaction> getUserTransactions(Long userId) {
        return transactionRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    public List<Transaction> getAccountTransactions(Long accountId) {
        Long nonNullAccountId = Objects.requireNonNull(accountId, "Account id is required");
        Account account = accountRepository.findById(nonNullAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        return transactionRepository.findByAccountOrderByTimestampDesc(account);
    }

    public List<Transaction> getAccountTransactionsForUser(Long accountId, Long userId) {
        Account account = accountRepository.findByIdAndUser_Id(accountId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        return transactionRepository.findByAccountOrderByTimestampDesc(account);
    }

    private void saveFailedTransaction(Account account, Type type, double amount, String description, String reason) {
        Transaction failed = new Transaction(account, type, amount, description == null || description.isBlank() ? "Failed transaction" : description);
        failed.setStatus(Status.FAILED);
        failed.setFailureReason(reason);
        transactionRepository.save(failed);
        LOGGER.warn("Transaction marked as failed. accountId={}, type={}, amount={}, reason={}",
                account.getId(), type, amount, reason);
    }
}

