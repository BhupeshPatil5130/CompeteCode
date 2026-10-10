package com.CompetCode.CompetCode.controller;

import com.CompetCode.CompetCode.dto.AuthResponse;
import com.CompetCode.CompetCode.dto.LoginRequest;
import com.CompetCode.CompetCode.dto.RegisterRequest;
import com.CompetCode.CompetCode.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody com.CompetCode.CompetCode.dto.RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }
}
