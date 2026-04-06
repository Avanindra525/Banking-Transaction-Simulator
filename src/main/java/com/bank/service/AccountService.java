
package com.bank.service;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.bank.model.Account;
import com.bank.model.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.UserRepository;

@Service
public class AccountService {

@Autowired
AccountRepository accountRepository;

@Autowired
UserRepository userRepository;

public Account openAccount(Long userId, String accountType, double initialDeposit) {
    Assert.notNull(userId, "User id is required");
    User user = userRepository.findById(Objects.requireNonNull(userId, "User id is required"))
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (!StringUtils.hasText(accountType)) {
        throw new IllegalArgumentException("Account type is required");
    }

    if (initialDeposit < 0) {
        throw new IllegalArgumentException("Initial deposit cannot be negative");
    }

    Account account = new Account();
    account.setUser(user);
    account.setName(user.getFullName());
    account.setEmail(user.getEmail());
    account.setAccountType(accountType.trim().toUpperCase());
    account.setBalance(initialDeposit);
    return accountRepository.save(account);
}

public List<Account> getAccountsForUser(Long userId) {
    return accountRepository.findByUser_IdOrderByIdDesc(userId);
}

public Account getUserAccount(Long accountId, Long userId) {
    return accountRepository.findByIdAndUser_Id(accountId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
}
}
