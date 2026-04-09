package com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.exceptions.model;

public record ErrorResponse(
        String message,
        String timestamp,
        int status
) {
}
