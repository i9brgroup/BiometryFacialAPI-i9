package com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record AuthenticationDataRequest(
        @Email
        @NotNull
        String email,
        @NotNull
        String password
) {
}
