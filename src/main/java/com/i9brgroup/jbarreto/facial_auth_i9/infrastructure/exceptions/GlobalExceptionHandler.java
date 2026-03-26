package com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.exceptions;

import com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.exceptions.model.ErrorResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.exceptions.model.InvalidCredentialsException;
import com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.exceptions.model.TemplatesNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.channels.ClosedChannelException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        ErrorResponse error = new ErrorResponse(
                "O tipo de conteúdo (Content-Type) enviado não é suportado. Certifique-se de usar 'application/json' para o payload.",
                LocalDateTime.now().format(formatter),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()
        );
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(error);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlerUserNotFound(EntityNotFoundException ex){
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                LocalDateTime.now().format(formatter),
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                LocalDateTime.now().format(formatter),
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ClosedChannelException.class)
    public ResponseEntity<ErrorResponse> handleClosedChannelException(ClosedChannelException ex){
        ErrorResponse error = new ErrorResponse(
                "Erro ao comunicar com o serviço externo. Por favor, tente novamente mais tarde.",
                LocalDateTime.now().format(formatter),
                HttpStatus.SERVICE_UNAVAILABLE.value()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException ex){
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                LocalDateTime.now().format(formatter),
                HttpStatus.UNAUTHORIZED.value()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(TemplatesNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTemplatesNotFoundException(TemplatesNotFoundException ex){
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                LocalDateTime.now().format(formatter),
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                LocalDateTime.now().format(formatter),
                HttpStatus.NOT_FOUND.value()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        ErrorResponse error = new ErrorResponse(
                "Credenciais inválidas.",
                LocalDateTime.now().format(formatter),
                HttpStatus.UNAUTHORIZED.value()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}
