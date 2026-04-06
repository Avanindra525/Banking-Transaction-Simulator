package com.bank.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.dto.ReportResponseDto;
import com.bank.model.Transaction;
import com.bank.security.AccessValidator;
import com.bank.service.ReportService;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final AccessValidator accessValidator;

    public ReportController(ReportService reportService, AccessValidator accessValidator) {
        this.reportService = reportService;
        this.accessValidator = accessValidator;
    }

    @GetMapping("/transactions")
    public ResponseEntity<ReportResponseDto> getTransactionReport(
            @RequestParam Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {
        accessValidator.validateUserAccess(userId);
        return ResponseEntity.ok(reportService.getReport(userId, type, fromDate, toDate));
    }

    @GetMapping(value = "/transactions/csv", produces = "text/csv")
    public ResponseEntity<String> downloadReportCsv(
            @RequestParam Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {
        accessValidator.validateUserAccess(userId);
        ReportResponseDto report = reportService.getReport(userId, type, fromDate, toDate);
        String csvContent = buildCsv(report.getTransactions());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions-report.csv")
                .contentType(MediaType.valueOf("text/csv"))
                .body(csvContent);
    }

    private String buildCsv(List<Transaction> transactions) {
        StringBuilder csv = new StringBuilder();
        csv.append("Transaction ID,Type,Amount,Date Time,Status,Failure Reason\n");
        for (Transaction txn : transactions) {
            csv.append(txn.getId()).append(',')
                    .append(txn.getType()).append(',')
                    .append(String.format("%.2f", txn.getAmount())).append(',')
                    .append('"').append(txn.getTimestamp()).append('"').append(',')
                    .append(txn.getStatus()).append(',')
                    .append('"').append(escapeCsv(txn.getFailureReason())).append('"')
                    .append('\n');
        }
        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\"\"");
    }
}
