package com.bank.service;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bank.dto.TransactionRequest;
import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.model.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AlertService alertService;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void processTransaction_shouldSaveSuccessDeposit() {
        Account account = new Account();
        User user = new User();
        user.setId(3L);
        account.setId(10L);
        account.setUser(user);
        account.setBalance(1000.0);

        TransactionRequest request = new TransactionRequest();
        request.setAccountId(10L);
        request.setType("DEPOSIT");
        request.setAmount(500.0);

        when(accountRepository.findById(10L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.processTransaction(request, 3L);

        Assertions.assertEquals(Transaction.Status.SUCCESS, result.getStatus());
        Assertions.assertEquals(1500.0, account.getBalance());
        verify(alertService, times(1)).triggerLargeTransactionAlertIfNeeded(any(Account.class), any(Transaction.class));
        verify(alertService, times(1)).triggerLowBalanceAlertIfNeeded(any(Account.class));
    }

    @Test
    void processTransaction_shouldPersistFailedStatusWhenInsufficientBalance() {
        Account account = new Account();
        User user = new User();
        user.setId(7L);
        account.setId(22L);
        account.setUser(user);
        account.setBalance(200.0);

        TransactionRequest request = new TransactionRequest();
        request.setAccountId(22L);
        request.setType("WITHDRAW");
        request.setAmount(800.0);

        when(accountRepository.findById(22L)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.processTransaction(request, 7L));

        Assertions.assertEquals("Insufficient balance", ex.getMessage());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(captor.capture());
        Assertions.assertEquals(Transaction.Status.FAILED, captor.getValue().getStatus());
    }

    @Test
    void processTransaction_shouldTransferAndCreateTransferInRecord() {
        User user = new User();
        user.setId(9L);

        Account from = new Account();
        from.setId(30L);
        from.setUser(user);
        from.setBalance(5000.0);

        Account to = new Account();
        to.setId(31L);
        to.setUser(user);
        to.setBalance(1500.0);

        TransactionRequest request = new TransactionRequest();
        request.setAccountId(30L);
        request.setToAccountId(31L);
        request.setType("TRANSFER_OUT");
        request.setAmount(1200.0);
        request.setDescription("Move to savings");

        when(accountRepository.findById(30L)).thenReturn(Optional.of(from));
        when(accountRepository.findById(31L)).thenReturn(Optional.of(to));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.processTransaction(request, 9L);

        Assertions.assertEquals(Transaction.Type.TRANSFER_OUT, result.getType());
        Assertions.assertEquals(Transaction.Status.SUCCESS, result.getStatus());
        Assertions.assertEquals(3800.0, from.getBalance());
        Assertions.assertEquals(2700.0, to.getBalance());
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(alertService, times(2)).triggerLargeTransactionAlertIfNeeded(any(Account.class), any(Transaction.class));
        verify(alertService, times(2)).triggerLowBalanceAlertIfNeeded(any(Account.class));
    }

    @Test
    void processTransaction_shouldPersistFailedTransferWhenTargetMissing() {
        User user = new User();
        user.setId(9L);

        Account from = new Account();
        from.setId(40L);
        from.setUser(user);
        from.setBalance(5000.0);

        TransactionRequest request = new TransactionRequest();
        request.setAccountId(40L);
        request.setToAccountId(99L);
        request.setType("TRANSFER_OUT");
        request.setAmount(200.0);

        when(accountRepository.findById(40L)).thenReturn(Optional.of(from));
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.processTransaction(request, 9L));

        Assertions.assertEquals("Target account not found", ex.getMessage());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(captor.capture());
        Assertions.assertEquals(Transaction.Status.FAILED, captor.getValue().getStatus());
        Assertions.assertEquals("Target account not found", captor.getValue().getFailureReason());
        verify(alertService, never()).triggerLowBalanceAlertIfNeeded(any(Account.class));
    }
}
