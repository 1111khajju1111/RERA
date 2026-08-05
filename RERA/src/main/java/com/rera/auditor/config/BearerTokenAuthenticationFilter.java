package com.rera.auditor.config;

import com.rera.auditor.entity.AuthToken;
import com.rera.auditor.entity.User;
import com.rera.auditor.repository.AuthTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Reads "Authorization: Bearer <token>", looks it up in auth_tokens, and
 * populates the SecurityContext for this request only — nothing is
 * persisted server-side per-request (SecurityConfig runs STATELESS), so
 * every request re-authenticates from the header.
 */
@Component
public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {

    private final AuthTokenRepository authTokenRepository;

    public BearerTokenAuthenticationFilter(AuthTokenRepository authTokenRepository) {
        this.authTokenRepository = authTokenRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String tokenValue = header.substring(7).trim();
            Optional<AuthToken> tokenOpt = authTokenRepository.findByToken(tokenValue);

            if (tokenOpt.isPresent() && tokenOpt.get().getExpiresAt().isAfter(LocalDateTime.now())) {
                User user = tokenOpt.get().getUser();
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
                var authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            // An invalid/expired token is deliberately NOT rejected here with
            // a 401 — it's just left unauthenticated, and Spring Security's
            // authorizeHttpRequests rule (anyRequest().authenticated()) is
            // what actually returns 401/403 for protected endpoints.
        }

        filterChain.doFilter(request, response);
    }
}
