package com.bank.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.model.Alert;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByAccount_IdAndAccount_User_IdOrderByCreatedAtDesc(Long accountId, Long userId);

    List<Alert> findByAccount_User_IdOrderByCreatedAtDesc(Long userId);
}
