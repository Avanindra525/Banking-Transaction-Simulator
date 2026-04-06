package com.bank.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.bank.model.User;
import com.bank.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User saveUser(User user) {
        Assert.notNull(user, "User is required");
        return userRepository.save(Objects.requireNonNull(user, "User is required"));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean isPasswordMatch(String rawPassword, String hashedPassword) {
        if (hashedPassword == null || hashedPassword.isBlank()) {
            return false;
        }
        if (isBcryptHash(hashedPassword)) {
            return passwordEncoder.matches(rawPassword, hashedPassword);
        }
        return legacyHash(rawPassword).equals(hashedPassword);
    }

    public boolean isLegacyPasswordMatch(String rawPassword, String hashedPassword) {
        return legacyHash(rawPassword).equals(hashedPassword);
    }

    public boolean isBcryptHash(String hashedPassword) {
        return hashedPassword.startsWith("$2a$") || hashedPassword.startsWith("$2b$") || hashedPassword.startsWith("$2y$");
    }

    private String legacyHash(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Legacy password hashing failed", ex);
        }
    }
}
