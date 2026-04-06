package com.bank.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUser_IdOrderByIdDesc(Long userId);
    Optional<Account> findByIdAndUser_Id(Long id, Long userId);
}
