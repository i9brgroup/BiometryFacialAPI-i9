package com.i9brgroup.jbarreto.facial_auth_i9.infra.exception.model.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        String message,
        String timestamp,
        int status
) {
}
