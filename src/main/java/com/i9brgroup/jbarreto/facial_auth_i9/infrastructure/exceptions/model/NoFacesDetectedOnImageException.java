package com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.exceptions.model;

public class NoFacesDetectedOnImageException extends RuntimeException {

    public NoFacesDetectedOnImageException(String message) {
        super(message);
    }

}
