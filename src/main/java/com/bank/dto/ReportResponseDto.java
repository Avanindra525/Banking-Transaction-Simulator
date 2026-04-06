package com.bank.dto;

import java.util.List;

import com.bank.model.Transaction;

public class ReportResponseDto {

    private List<Transaction> transactions;
    private double totalDeposits;
    private double totalWithdrawals;
    private double totalFailed;

    public ReportResponseDto() {
    }

    public ReportResponseDto(List<Transaction> transactions, double totalDeposits, double totalWithdrawals, double totalFailed) {
        this.transactions = transactions;
        this.totalDeposits = totalDeposits;
        this.totalWithdrawals = totalWithdrawals;
        this.totalFailed = totalFailed;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public double getTotalDeposits() {
        return totalDeposits;
    }

    public void setTotalDeposits(double totalDeposits) {
        this.totalDeposits = totalDeposits;
    }

    public double getTotalWithdrawals() {
        return totalWithdrawals;
    }

    public void setTotalWithdrawals(double totalWithdrawals) {
        this.totalWithdrawals = totalWithdrawals;
    }

    public double getTotalFailed() {
        return totalFailed;
    }

    public void setTotalFailed(double totalFailed) {
        this.totalFailed = totalFailed;
    }
}
