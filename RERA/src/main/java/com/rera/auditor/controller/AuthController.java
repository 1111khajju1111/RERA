package com.rera.auditor.controller;

import com.rera.auditor.dto.AuthResponse;
import com.rera.auditor.dto.LoginRequest;
import com.rera.auditor.dto.RegisterRequest;
import com.rera.auditor.dto.UserResponse;
import com.rera.auditor.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /** Returns the token in the response body — the frontend stores it and sends it back as "Authorization: Bearer <token>". */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = (authHeader != null && authHeader.startsWith("Bearer "))
            ? authHeader.substring(7).trim()
            : null;
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }
}
