package com.rera.auditor.config;

import com.rera.auditor.entity.User;
import com.rera.auditor.exception.AuthenticationFailedException;
import com.rera.auditor.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/** Resolves the logged-in User entity from the current session's SecurityContext. */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AuthenticationFailedException("No authenticated user in session");
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthenticationFailedException("Authenticated user not found: " + email));
    }
}
