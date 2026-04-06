package com.bank.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AccessValidator {

    public void validateUserAccess(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new SecurityException("Unauthorized request");
        }

        Long principalUserId = Long.valueOf(String.valueOf(authentication.getPrincipal()));
        if (!principalUserId.equals(userId)) {
            throw new SecurityException("Unauthorized request");
        }
    }
}
