package com.CompetCode.CompetCode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private Long userId;
    private String username;
    private String email;
    private Integer rating;
}
