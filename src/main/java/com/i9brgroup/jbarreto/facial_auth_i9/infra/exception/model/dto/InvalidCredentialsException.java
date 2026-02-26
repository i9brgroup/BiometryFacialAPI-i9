package com.i9brgroup.jbarreto.facial_auth_i9.infra.exception.model.dto;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
