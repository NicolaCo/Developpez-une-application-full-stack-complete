package com.openclassrooms.mddapi.authentication.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.openclassrooms.mddapi.common.user.CurrentUserPort;

@Service
public class SecurityCurrentUserProvider implements CurrentUserPort {

    @Override
    public Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser authUser)) {
            throw new IllegalStateException("No AuthUser in security context");
        }
        return authUser.id();
    }
}
