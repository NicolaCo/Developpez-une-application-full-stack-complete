package com.openclassrooms.mddapi.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
public class SecurityCurrentUserProvider {

    public Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser authUser)) {
            throw new IllegalStateException("No AuthUser in security context");
        }
        return authUser.id();
    }
}
