package com.rera.auditor.service;

import com.rera.auditor.dto.AuthResponse;
import com.rera.auditor.dto.LoginRequest;
import com.rera.auditor.dto.RegisterRequest;
import com.rera.auditor.dto.UserResponse;
import com.rera.auditor.entity.AuthToken;
import com.rera.auditor.entity.User;
import com.rera.auditor.exception.AuthenticationFailedException;
import com.rera.auditor.exception.DuplicateResourceException;
import com.rera.auditor.mapper.UserMapper;
import com.rera.auditor.repository.AuthTokenRepository;
import com.rera.auditor.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Bearer-token auth, replacing session cookies (see V7 migration for why:
 * frontend and backend are on unrelated domains, so the session cookie
 * was classified as third-party by browsers and blocked outright in
 * Safari/Firefox/Brave/Incognito regardless of SameSite/Secure config).
 *
 * This is a deliberately simple opaque token — not JWT. A random 32-byte
 * value stored in `auth_tokens`, looked up on every request. Simpler to
 * revoke (just delete the row) than a JWT, and doesn't need a signing
 * secret to manage. Trade-off: every authenticated request costs one extra
 * DB lookup compared to a self-contained JWT — fine at this project's
 * scale, worth revisiting only if the token lookup becomes a measured
 * bottleneck.
 */
@Service
public class AuthService {

    private static final long TOKEN_VALIDITY_DAYS = 30;
    private final SecureRandom secureRandom = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AuthTokenRepository authTokenRepository;
    private final UserMapper userMapper;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                        AuthenticationManager authenticationManager,
                        AuthTokenRepository authTokenRepository,
                        UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.authTokenRepository = authTokenRepository;
        this.userMapper = userMapper;
    }

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole("ARCHITECT");
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authRequest = new UsernamePasswordAuthenticationToken(request.email(), request.password());
            authenticationManager.authenticate(authRequest); // throws if credentials are wrong

            User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthenticationFailedException("User not found"));

            String tokenValue = generateToken();
            AuthToken token = new AuthToken();
            token.setToken(tokenValue);
            token.setUser(user);
            token.setExpiresAt(LocalDateTime.now().plusDays(TOKEN_VALIDITY_DAYS));
            authTokenRepository.save(token);

            return new AuthResponse(userMapper.toResponse(user), tokenValue);
        } catch (AuthenticationFailedException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AuthenticationFailedException("Invalid email or password");
        }
    }

    @Transactional
    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            authTokenRepository.deleteByToken(token);
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
