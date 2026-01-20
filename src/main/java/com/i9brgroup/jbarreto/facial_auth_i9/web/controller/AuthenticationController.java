package com.i9brgroup.jbarreto.facial_auth_i9.web.controller;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.UserLoginEntity;
import com.i9brgroup.jbarreto.facial_auth_i9.infra.security.service.TokenService;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request.AuthenticationDataRequest;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.TokenResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthenticationController(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthenticationDataRequest userDataDTO){
        var authenticationToken = new UsernamePasswordAuthenticationToken(userDataDTO.email(), userDataDTO.password());
        var auth = authenticationManager.authenticate(authenticationToken);
        var user = (UserLoginEntity) auth.getPrincipal();

        if (user == null) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }
        var tokenString = tokenService.generateToken(user);

        return ResponseEntity.ok().body(new TokenResponse(tokenString));
    }
}
