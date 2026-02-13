package com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.auth.UserLoginEntity;

import java.time.LocalDateTime;

public record UserLoginResponse(
        String username,
        String email,
        String siteId,
        LocalDateTime crreatedAt
) {
    public UserLoginResponse(UserLoginEntity user) {
        this(user.getUsername(), user.getEmail(), user.getSiteId(), user.getCreatedAt());
    }
}
