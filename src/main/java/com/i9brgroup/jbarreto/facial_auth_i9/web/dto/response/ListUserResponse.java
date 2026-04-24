package com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.enums.UserRoles;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.auth.UserLoginEntity;

import java.time.LocalDateTime;

public record ListUserResponse(
        Long id,
        String username,
        String email,
        String password,
        LocalDateTime createdAt,
        String siteId,
        UserRoles role,
        Boolean ativo
) {
    public ListUserResponse(UserLoginEntity userLoginEntity) {
        this(
                userLoginEntity.getId(),
                userLoginEntity.getUsername(),
                userLoginEntity.getEmail(),
                userLoginEntity.getPassword(),
                userLoginEntity.getCreatedAt(),
                userLoginEntity.getSiteId(),
                userLoginEntity.getRole(),
                userLoginEntity.getAtivo()
        );
    }
}
