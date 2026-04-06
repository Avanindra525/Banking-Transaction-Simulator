package com.bank.service;

import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.bank.dto.EmailRequest;
import com.bank.dto.EmailResponse;

@Service
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Set<String> SUPPORTED_EVENTS = Set.of(
            "TRANSACTION_SUCCESS",
            "LOW_BALANCE",
            "LARGE_TRANSACTION",
            "ACCOUNT_CREATED");

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailService(JavaMailSender mailSender, @Value("${bank.mail.from:no-reply@inbank.local}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public EmailResponse sendEmail(EmailRequest request) {
        if (request == null || !StringUtils.hasText(request.getEmail())) {
            return new EmailResponse(false, "Email failed to send");
        }

        if (!EMAIL_PATTERN.matcher(request.getEmail().trim()).matches()) {
            return new EmailResponse(false, "Email failed to send");
        }

        if (!StringUtils.hasText(request.getEventType())) {
            return new EmailResponse(false, "Email event type is required");
        }

        String normalizedEvent = request.getEventType().trim().toUpperCase();
        if (!SUPPORTED_EVENTS.contains(normalizedEvent)) {
            return new EmailResponse(false, "Unsupported email event type");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(request.getEmail().trim());
            message.setSubject("INBANK Notification: " + normalizedEvent.replace('_', ' '));
            message.setText(StringUtils.hasText(request.getContent())
                    ? request.getContent().trim()
                    : "You have a new banking notification.");
            mailSender.send(message);
        } catch (MailException ex) {
            LOGGER.error("Failed to send email for event {}", normalizedEvent, ex);
            return new EmailResponse(false, "Email failed to send");
        }

        return new EmailResponse(true, "Email notification sent successfully");
    }

    public void sendAlertEmail(String email, String eventType, String content) {
        EmailRequest request = new EmailRequest();
        request.setEmail(email);
        request.setEventType(eventType);
        request.setContent(content);
        sendEmail(request);
    }
}
