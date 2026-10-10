package com.CompetCode.CompetCode.dto;

import com.CompetCode.CompetCode.entity.User;

public record UserSummaryDto(
        Long id,
        String username,
        Integer rating
) {
    public static UserSummaryDto from(User user) {
        if (user == null) return null;
        return new UserSummaryDto(user.getId(), user.getUsername(), user.getRating());
    }
}
