package com.bank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.bank.dto.AuthResponse;
import com.bank.dto.LoginRequest;
import com.bank.dto.SignupRequest;
import com.bank.model.User;
import com.bank.security.JwtService;

@Service
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    public AuthResponse signup(SignupRequest request) {
        if (!isValidSignupRequest(request)) {
            return new AuthResponse(false, "Full name, email, and password are required", null, null);
        }

        String email = request.getEmail().trim().toLowerCase();
        if (userService.existsByEmail(email)) {
            return new AuthResponse(false, "Email is already registered", null, email);
        }

        String passwordHash = userService.hashPassword(request.getPassword().trim());
        User user = new User(request.getFullName().trim(), email, passwordHash);
        userService.saveUser(user);

        AuthResponse response = new AuthResponse(true, "Signup successful", request.getFullName().trim(), email);
        response.setUserId(user.getId());
        response.setToken(jwtService.generateToken(user));
        response.setExpiresAt(jwtService.getTokenExpiryEpochMillis());
        return response;
    }

    public AuthResponse login(LoginRequest request) {
        if (request == null || !StringUtils.hasText(request.getEmail()) || !StringUtils.hasText(request.getPassword())) {
            return new AuthResponse(false, "Email and password are required", null, null);
        }

        String email = request.getEmail().trim().toLowerCase();
        return userService.findByEmail(email)
                .map(user -> {
                    String rawPassword = request.getPassword().trim();
                    boolean matched = userService.isPasswordMatch(rawPassword, user.getPasswordHash());
                    if (!matched) {
                        return new AuthResponse(false, "Invalid email or password", null, email);
                    }

                    // Upgrade old SHA-256 hashes to BCrypt after successful legacy login.
                    if (!userService.isBcryptHash(user.getPasswordHash())
                            && userService.isLegacyPasswordMatch(rawPassword, user.getPasswordHash())) {
                        user.setPasswordHash(userService.hashPassword(rawPassword));
                        userService.saveUser(user);
                    }

                    AuthResponse response = new AuthResponse(true, "Login successful", user.getFullName(), user.getEmail());
                    response.setUserId(user.getId());
                    response.setToken(jwtService.generateToken(user));
                    response.setExpiresAt(jwtService.getTokenExpiryEpochMillis());
                    return response;
                })
                .orElse(new AuthResponse(false, "Invalid email or password", null, email));
    }

    public java.util.List<User> getAllUsers() {
        return userService.findAllUsers();
    }

    private boolean isValidSignupRequest(SignupRequest request) {
        return request != null
                && StringUtils.hasText(request.getFullName())
                && StringUtils.hasText(request.getEmail())
                && StringUtils.hasText(request.getPassword());
    }
}

