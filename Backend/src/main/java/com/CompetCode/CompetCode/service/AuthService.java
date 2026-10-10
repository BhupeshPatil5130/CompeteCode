package com.CompetCode.CompetCode.service;

import com.CompetCode.CompetCode.dto.AuthResponse;
import com.CompetCode.CompetCode.dto.LoginRequest;
import com.CompetCode.CompetCode.dto.RegisterRequest;
import com.CompetCode.CompetCode.entity.User;
import com.CompetCode.CompetCode.repository.UserRepository;
import com.CompetCode.CompetCode.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .rating(1200)
                .build();

        user = userRepository.save(user);
        String token = jwtUtil.generateToken(user.getUsername(), user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getId());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .rating(user.getRating())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        String token = jwtUtil.generateToken(user.getUsername(), user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getId());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .rating(user.getRating())
                .build();
    }

    public AuthResponse refresh(com.CompetCode.CompetCode.dto.RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (refreshToken == null) {
            throw new IllegalArgumentException("Refresh token is required");
        }
        
        String username = jwtUtil.extractUsername(refreshToken);
        if (username == null || !"refresh".equals(jwtUtil.extractTokenType(refreshToken))) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                
        // Check if token is expired
        org.springframework.security.core.userdetails.UserDetails userDetails = 
            new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), java.util.Collections.emptyList());
            
        if (!jwtUtil.isTokenValid(refreshToken, userDetails)) {
            throw new IllegalArgumentException("Refresh token has expired or is invalid");
        }
        
        String newToken = jwtUtil.generateToken(user.getUsername(), user.getId());
        // Optionally rotate refresh token
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getId());

        return AuthResponse.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .rating(user.getRating())
                .build();
    }
}
