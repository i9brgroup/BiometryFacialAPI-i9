package com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.auth.UserLoginEntity;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

public record UserCreateResponse(
        String status,
        String username,
        String email,
        String siteId,
        @CreationTimestamp
        LocalDateTime createdAt
) {
    public UserCreateResponse(UserLoginEntity user) {
        this("success", user.getUsername(), user.getEmail(), user.getSiteId(), user.getCreatedAt());
    }
}
