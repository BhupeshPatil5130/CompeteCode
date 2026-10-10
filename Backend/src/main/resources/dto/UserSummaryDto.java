package com.CompetCode.CompetCode.dto;

import com.CompetCode.CompetCode.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDto {
    private Long id;
    private String username;
    private Integer rating;

    public static UserSummaryDto from(User user) {
        if (user == null) return null;
        return UserSummaryDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .rating(user.getRating())
                .build();
    }
}
