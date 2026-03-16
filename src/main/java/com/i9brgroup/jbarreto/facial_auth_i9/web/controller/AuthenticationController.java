package com.i9brgroup.jbarreto.facial_auth_i9.web.controller;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.UserServiceImpl;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request.AuthenticationDataRequest;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.TokenResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final UserServiceImpl userAuthentication;

    public AuthenticationController(UserServiceImpl userAuthentication) {
        this.userAuthentication = userAuthentication;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthenticationDataRequest userDataDTO){
        var tokenString = userAuthentication.login(userDataDTO);

        return ResponseEntity.ok().body(new TokenResponse(tokenString));
    }
}
