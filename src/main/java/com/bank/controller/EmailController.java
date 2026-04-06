package com.bank.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.dto.EmailRequest;
import com.bank.dto.EmailResponse;
import com.bank.security.AccessValidator;
import com.bank.service.EmailService;

@RestController
@RequestMapping("/email")
public class EmailController {

    private final EmailService emailService;
    private final AccessValidator accessValidator;

    public EmailController(EmailService emailService, AccessValidator accessValidator) {
        this.emailService = emailService;
        this.accessValidator = accessValidator;
    }

    @PostMapping("/send")
    public ResponseEntity<EmailResponse> send(@RequestBody EmailRequest request) {
        accessValidator.validateUserAccess(request.getUserId());
        EmailResponse response = emailService.sendEmail(request);
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.ok(response);
    }
}
