package com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.exceptions.model;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
