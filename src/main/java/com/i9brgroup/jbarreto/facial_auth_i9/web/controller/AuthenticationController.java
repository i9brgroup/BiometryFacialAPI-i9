package com.i9brgroup.jbarreto.facial_auth_i9.web.controller;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.TokenService;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.UserServiceImpl;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request.AuthenticationDataRequest;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.AccessTokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final UserServiceImpl userAuthentication;
    private final TokenService tokenService;

    public AuthenticationController(UserServiceImpl userAuthentication, TokenService tokenService) {
        this.userAuthentication = userAuthentication;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthenticationDataRequest userDataDTO){
        var tokens = userAuthentication.signIn(userDataDTO);

        var maxAge = 7 * 24 * 60 * 60; // 7 days in seconds

        var cookie = tokenService.cookieConfig("refreshToken", tokens.refreshToken(), maxAge); // 7 days

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AccessTokenResponse(tokens.accessToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(value = "refreshToken", required = false) String refreshToken){
        if (refreshToken == null) {
            return ResponseEntity.status(401).body("Refresh token is missing");
        }

        var tokens = userAuthentication.refresh(refreshToken);

        var maxAge = 7 * 24 * 60 * 60; // 7 days in seconds

        var cookie = tokenService.cookieConfig("refreshToken", tokens.refreshToken(), maxAge); // 7 days

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AccessTokenResponse(tokens.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request){
        var refreshToken = tokenService.extractRefreshTokenFromRequest(request);

        if (refreshToken != null) {
            tokenService.putTokenOnBlacklist(refreshToken);
        }

        var cleanCookie = tokenService.clearCookie();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .build();
    }
}
