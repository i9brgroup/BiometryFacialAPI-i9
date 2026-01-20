package com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.enums.UserRoles;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

public record UserLoginRequest(
        @Valid
        @NotNull
        String username,
        @Email
        String email,
        @NotNull
        String password,
        @NotNull
        String siteId,
        UserRoles roles
) {
}
