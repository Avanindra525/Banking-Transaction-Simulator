package com.bank.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bank.dto.ReportResponseDto;
import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.model.User;
import com.bank.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void getReport_shouldCalculateTotals() {
        User user = new User();
        user.setId(4L);

        Account account = new Account();
        account.setId(12L);
        account.setUser(user);

        Transaction deposit = new Transaction(account, Transaction.Type.DEPOSIT, 5000, "deposit");
        deposit.setStatus(Transaction.Status.SUCCESS);
        deposit.setTimestamp(LocalDateTime.now().minusDays(1));

        Transaction withdraw = new Transaction(account, Transaction.Type.WITHDRAW, 1200, "withdraw");
        withdraw.setStatus(Transaction.Status.SUCCESS);
        withdraw.setTimestamp(LocalDateTime.now().minusHours(10));

        Transaction failed = new Transaction(account, Transaction.Type.WITHDRAW, 900, "failed");
        failed.setStatus(Transaction.Status.FAILED);
        failed.setTimestamp(LocalDateTime.now().minusHours(5));

        when(transactionRepository.findFilteredTransactionsByUser(4L, null, null, null))
                .thenReturn(List.of(deposit, withdraw, failed));

        ReportResponseDto report = reportService.getReport(4L, "ALL", null, null);

        Assertions.assertEquals(5000, report.getTotalDeposits());
        Assertions.assertEquals(1200, report.getTotalWithdrawals());
        Assertions.assertEquals(900, report.getTotalFailed());
        Assertions.assertEquals(3, report.getTransactions().size());
    }

    @Test
    void getReport_shouldPassDateAndTypeFiltersToRepository() {
        when(transactionRepository.findFilteredTransactionsByUser(4L, Transaction.Type.WITHDRAW,
                LocalDate.of(2026, 3, 1).atStartOfDay(),
                LocalDate.of(2026, 3, 10).atTime(23, 59, 59, 999999999)))
                .thenReturn(List.of());

        ReportResponseDto report = reportService.getReport(4L, "WITHDRAW",
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 10));

        Assertions.assertEquals(0, report.getTransactions().size());
        verify(transactionRepository).findFilteredTransactionsByUser(4L, Transaction.Type.WITHDRAW,
                LocalDate.of(2026, 3, 1).atStartOfDay(),
                LocalDate.of(2026, 3, 10).atTime(23, 59, 59, 999999999));
    }

    @Test
    void getReport_shouldRejectInvalidTypeFilter() {
        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> reportService.getReport(4L, "bad_type", null, null));

        Assertions.assertEquals("Invalid transaction type filter", ex.getMessage());
    }
}
