package com.CompetCode.CompetCode.dto;

public record AuthResponse(
        String token,
        String refreshToken,
        Long userId,
        String username,
        String email,
        Integer rating
) {}
