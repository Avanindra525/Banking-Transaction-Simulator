package com.bank.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bank.model.Account;
import com.bank.model.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void openAccount_shouldCreateAccountWhenInputIsValid() {
        User user = new User();
        user.setId(1L);
        user.setFullName("Alice");
        user.setEmail("alice@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.save(org.mockito.ArgumentMatchers.any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Account result = accountService.openAccount(1L, "SAVINGS", 1000.0);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("Alice", result.getName());
        Assertions.assertEquals("alice@example.com", result.getEmail());
        Assertions.assertEquals("SAVINGS", result.getAccountType());
        Assertions.assertEquals(1000.0, result.getBalance());
    }

    @Test
    void openAccount_shouldThrowWhenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> accountService.openAccount(99L, "SAVINGS", 100.0));

        Assertions.assertEquals("User not found", ex.getMessage());
    }

    @Test
    void getAccountsForUser_shouldReturnAccounts() {
        Account first = new Account(10L, "Alice", "alice@example.com", "SAVINGS", 1200.0);
        Account second = new Account(11L, "Alice", "alice@example.com", "CURRENT", 400.0);
        when(accountRepository.findByUser_IdOrderByIdDesc(1L)).thenReturn(List.of(first, second));

        List<Account> result = accountService.getAccountsForUser(1L);

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(Long.valueOf(10L), result.get(0).getId());
    }

    @Test
    void getUserAccount_shouldReturnAccountWhenOwnedByUser() {
        Account existing = new Account(5L, "Charlie", "charlie@example.com", "SAVINGS", 700.0);
        when(accountRepository.findByIdAndUser_Id(5L, 2L)).thenReturn(Optional.of(existing));

        Account result = accountService.getUserAccount(5L, 2L);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(Long.valueOf(5L), result.getId());
    }

    @Test
    void getUserAccount_shouldThrowWhenMissing() {
        when(accountRepository.findByIdAndUser_Id(99L, 1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> accountService.getUserAccount(99L, 1L));

        Assertions.assertEquals("Account not found", ex.getMessage());
    }
}
