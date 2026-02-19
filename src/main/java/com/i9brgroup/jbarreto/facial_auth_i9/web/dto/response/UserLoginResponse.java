package com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.auth.UserLoginEntity;

import java.time.LocalDateTime;

public record UserLoginResponse(
        String status,
        String username,
        String email,
        String siteId,
        LocalDateTime createdAt
) {
    public UserLoginResponse(UserLoginEntity user) {
        this("success", user.getUsername(), user.getEmail(), user.getSiteId(), user.getCreatedAt());
    }
}
