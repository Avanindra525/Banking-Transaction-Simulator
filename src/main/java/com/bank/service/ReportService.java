package com.bank.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.bank.dto.ReportResponseDto;
import com.bank.model.Transaction;
import com.bank.model.Transaction.Status;
import com.bank.model.Transaction.Type;
import com.bank.repository.TransactionRepository;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public ReportResponseDto getReport(Long userId, String type, LocalDate fromDate, LocalDate toDate) {
        Type parsedType = parseType(type);
        LocalDateTime fromDateTime = fromDate == null ? null : fromDate.atStartOfDay();
        LocalDateTime toDateTime = toDate == null ? null : toDate.atTime(LocalTime.MAX);

        List<Transaction> transactions = transactionRepository.findFilteredTransactionsByUser(
                userId,
                parsedType,
                fromDateTime,
                toDateTime);

        double totalDeposits = transactions.stream()
                .filter(txn -> txn.getStatus() == Status.SUCCESS && txn.getType() == Type.DEPOSIT)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalWithdrawals = transactions.stream()
                .filter(txn -> txn.getStatus() == Status.SUCCESS && txn.getType() == Type.WITHDRAW)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalFailed = transactions.stream()
                .filter(txn -> txn.getStatus() == Status.FAILED)
                .mapToDouble(Transaction::getAmount)
                .sum();

        return new ReportResponseDto(transactions, totalDeposits, totalWithdrawals, totalFailed);
    }

    private Type parseType(String rawType) {
        if (!StringUtils.hasText(rawType) || "ALL".equalsIgnoreCase(rawType.trim())) {
            return null;
        }
        try {
            return Type.valueOf(rawType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid transaction type filter");
        }
    }
}
