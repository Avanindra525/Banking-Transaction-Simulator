package com.bank.dto;

public class TransactionRequest {
    private Long accountId;
    private String type; // DEPOSIT, WITHDRAW, TRANSFER
    private double amount;
    private String description;
    private Long toAccountId; // for TRANSFER

    // Constructors, getters, setters
    public TransactionRequest() {}

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getToAccountId() { return toAccountId; }
    public void setToAccountId(Long toAccountId) { this.toAccountId = toAccountId; }
}

