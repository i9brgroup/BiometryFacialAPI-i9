package com.i9brgroup.jbarreto.facial_auth_i9.infra.exception;

import com.i9brgroup.jbarreto.facial_auth_i9.infra.exception.model.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        ErrorResponse error = new ErrorResponse(
                "O tipo de conteúdo (Content-Type) enviado não é suportado. Certifique-se de usar 'application/json' para o payload.",
                LocalDateTime.now(),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()
        );
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(error);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlerUserNotFound(UsernameNotFoundException ex){
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
