package com.bank.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bank.dto.AlertDto;
import com.bank.model.Account;
import com.bank.model.Alert;
import com.bank.model.Transaction;
import com.bank.model.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.AlertRepository;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AlertServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private EmailService emailService;

    private AlertService alertService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        alertService = new AlertService(accountRepository, alertRepository, emailService, 1000, 10000);
    }

    @Test
    void getAlertsForAccount_shouldReturnPersistedAlerts() {
        User user = new User();
        user.setId(2L);

        Account account = new Account();
        account.setId(10L);
        account.setUser(user);

        Alert alert = new Alert(account, Alert.Type.LOW_BALANCE, "Low Balance", "Low balance detected", "HIGH");

        when(accountRepository.findByIdAndUser_Id(10L, 2L)).thenReturn(Optional.of(account));
        when(alertRepository.findByAccount_IdAndAccount_User_IdOrderByCreatedAtDesc(10L, 2L)).thenReturn(List.of(alert));

        List<AlertDto> result = alertService.getAlertsForAccount(10L, 2L);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Low Balance", result.get(0).getTitle());
    }

    @Test
    @SuppressWarnings("null")
    void triggerLargeTransactionAlertIfNeeded_shouldPersistAndSendEmail() {
        User user = new User();
        user.setId(5L);

        Account account = new Account();
        account.setId(22L);
        account.setUser(user);
        account.setEmail("demo@bank.com");

        Transaction transaction = new Transaction();
        transaction.setAmount(12000);
        transaction.setType(Transaction.Type.DEPOSIT);
        transaction.setStatus(Transaction.Status.SUCCESS);

        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        alertService.triggerLargeTransactionAlertIfNeeded(account, transaction);

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository, times(1)).save(captor.capture());
        verify(emailService, times(1)).sendAlertEmail(any(), any(), any());
        Assertions.assertEquals(Alert.Type.LARGE_TRANSACTION, captor.getValue().getType());
    }

    @Test
    @SuppressWarnings("null")
    void triggerLowBalanceAlertIfNeeded_shouldPersistAndSendEmail() {
        User user = new User();
        user.setId(5L);

        Account account = new Account();
        account.setId(23L);
        account.setUser(user);
        account.setEmail("demo@bank.com");
        account.setBalance(400.0);

        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        alertService.triggerLowBalanceAlertIfNeeded(account);

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository, times(1)).save(captor.capture());
        verify(emailService, times(1)).sendAlertEmail(any(), any(), any());
        Assertions.assertEquals(Alert.Type.LOW_BALANCE, captor.getValue().getType());
    }

    @Test
    void triggerLowBalanceAlertIfNeeded_shouldSkipWhenBalanceIsAboveThreshold() {
        Account account = new Account();
        account.setBalance(1500.0);

        alertService.triggerLowBalanceAlertIfNeeded(account);

        verify(alertRepository, never()).save(any(Alert.class));
        verify(emailService, never()).sendAlertEmail(any(), any(), any());
    }

    @Test
    void getAlertsForUser_shouldMapPersistedAlerts() {
        User user = new User();
        user.setId(7L);
        Account account = new Account();
        account.setId(88L);
        account.setUser(user);

        Alert alert = new Alert(account, Alert.Type.LARGE_TRANSACTION, "Large Transaction", "Txn", "MEDIUM");

        when(alertRepository.findByAccount_User_IdOrderByCreatedAtDesc(7L)).thenReturn(List.of(alert));

        List<AlertDto> result = alertService.getAlertsForUser(7L);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(Long.valueOf(88L), result.get(0).getAccountId());
    }
}
