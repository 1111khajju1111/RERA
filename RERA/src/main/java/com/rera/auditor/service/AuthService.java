package com.rera.auditor.service;

import com.rera.auditor.dto.LoginRequest;
import com.rera.auditor.dto.RegisterRequest;
import com.rera.auditor.dto.UserResponse;
import com.rera.auditor.entity.User;
import com.rera.auditor.exception.AuthenticationFailedException;
import com.rera.auditor.exception.DuplicateResourceException;
import com.rera.auditor.mapper.UserMapper;
import com.rera.auditor.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UserMapper userMapper;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                        AuthenticationManager authenticationManager,
                        SecurityContextRepository securityContextRepository,
                        UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
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

    /** Authenticates and persists the SecurityContext into the HTTP session. */
    public UserResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try {
            Authentication authRequest = new UsernamePasswordAuthenticationToken(request.email(), request.password());
            Authentication authResult = authenticationManager.authenticate(authRequest);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authResult);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, httpRequest, httpResponse);

            User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthenticationFailedException("User not found"));
            return userMapper.toResponse(user);
        } catch (Exception ex) {
            throw new AuthenticationFailedException("Invalid email or password");
        }
    }

    public void logout(HttpServletRequest request) {
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        SecurityContextHolder.clearContext();
    }
}
