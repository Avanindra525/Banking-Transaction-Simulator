package com.bank.dto;

import java.time.LocalDateTime;

public class AlertDto {
    private Long accountId;
    private String title;
    private String message;
    private String severity;
    private LocalDateTime createdAt;

    public AlertDto() {
    }

    public AlertDto(Long accountId, String title, String message, String severity, LocalDateTime createdAt) {
        this.accountId = accountId;
        this.title = title;
        this.message = message;
        this.severity = severity;
        this.createdAt = createdAt;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
