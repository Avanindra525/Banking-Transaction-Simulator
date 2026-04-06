package com.bank.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.dto.AlertDto;
import com.bank.security.AccessValidator;
import com.bank.service.AlertService;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    private final AlertService alertService;
    private final AccessValidator accessValidator;

    public AlertController(AlertService alertService, AccessValidator accessValidator) {
        this.alertService = alertService;
        this.accessValidator = accessValidator;
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<List<AlertDto>> getAccountAlerts(@PathVariable Long accountId, @RequestParam Long userId) {
        accessValidator.validateUserAccess(userId);
        List<AlertDto> alerts = alertService.getAlertsForAccount(accountId, userId);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AlertDto>> getUserAlerts(@PathVariable Long userId) {
        accessValidator.validateUserAccess(userId);
        return ResponseEntity.ok(alertService.getAlertsForUser(userId));
    }
}
