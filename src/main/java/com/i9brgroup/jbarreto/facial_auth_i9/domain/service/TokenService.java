package com.i9brgroup.jbarreto.facial_auth_i9.domain.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.auth.RefreshToken;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.auth.UserLoginEntity;
import com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.exceptions.model.JwtManagerException;
import com.i9brgroup.jbarreto.facial_auth_i9.resources.repository.auth.UserRepository;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.TokenResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Getter
public class TokenService {

    @Value("${api.security.token.secret}")
    private String SECRET;

    private final UserRepository userRepository;
    private final RefreshTokenStore refreshTokenStore;

    public String createAccessToken(UserLoginEntity user){
        return createToken(user, 15, "accessToken", null);
    }

    public String createRefreshToken(UserLoginEntity user){
        long expireAt = Instant.now().plus(Duration.ofDays(7)).toEpochMilli();

        // Gerar UUID único para o refresh token
        UUID tokenId = UUID.randomUUID();

        var refreshTokenJWT = createToken(user, expireAt, "refreshToken", tokenId);

        var refreshTokenEntity = RefreshToken.builder()
                .id(tokenId)
                .tokenHash(refreshTokenJWT)
                .userId(user.getId())
                .expiresAt(Instant.ofEpochMilli(expireAt))
                .build();

        refreshTokenStore.store(refreshTokenEntity);

        return refreshTokenJWT;
    }


    public void putTokenOnBlacklist(UUID tokenId) {
        refreshTokenStore.revoke(tokenId);
    }


    public void putTokenOnBlacklist(String refreshTokenJWT) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            var verifier = JWT.require(algorithm)
                    .withIssuer("Facial_authI9_API")
                    .build();

            DecodedJWT decodedJWT = verifier.verify(refreshTokenJWT);
            String tokenIdString = decodedJWT.getClaim("tokenId").asString();

            if (tokenIdString != null && !tokenIdString.isEmpty()) {
                UUID tokenId = UUID.fromString(tokenIdString);
                putTokenOnBlacklist(tokenId);
            }
        } catch (Exception e) {
            // Log silenciosamente se não conseguir extrair o tokenId
            // O token provavelmente já foi invalidado ou é inválido
        }
    }

    public boolean validateRefreshToken(UUID tokenId) {
        if (refreshTokenStore.isBlackListed(tokenId)) {
            return false;
        }
        RefreshToken refreshToken = refreshTokenStore.get(tokenId);
        return refreshToken != null && refreshToken.getExpiresAt().isAfter(Instant.now());
    }


    private String createToken(UserLoginEntity user, long expireAt, String tokenType, UUID tokenId) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            var builder = JWT.create()
                    .withIssuer("Facial_authI9_API")
                    .withSubject(user.getEmail())
                    .withClaim("Username", user.getUsername())
                    .withClaim("siteId", user.getSiteId())
                    .withClaim("role", user.getRole().toString())
                    .withClaim("type", tokenType);

            // Se for um refresh token, adicionar o UUID como claim
            if (tokenId != null && "refreshToken".equals(tokenType)) {
                builder.withClaim("tokenId", tokenId.toString());
            }

            return builder.withExpiresAt(expiresAt(expireAt))
                    .sign(algorithm);
        } catch (JWTCreationException exception){
            throw new JwtManagerException("Erro ao gerar token: " + exception.getMessage());
        }
    }

    public TokenResponse refresh(String refreshTokenJWT, UUID tokenId){
        try {
            // Validar se o token não está na blacklist e se é válido
            if (!validateRefreshToken(tokenId)) {
                throw new JwtManagerException("Token expirado");
            }

            // Recuperar a entidade RefreshToken do store
            RefreshToken userTokenOf = refreshTokenStore.get(tokenId);

            if (userTokenOf == null) {
                throw new JwtManagerException("Token expirado");
            }

            // Verificar a assinatura JWT e extrair informações
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            var verifier = JWT.require(algorithm)
                    .withIssuer("Facial_authI9_API")
                    .build();

            DecodedJWT decodedJWT = verifier.verify(refreshTokenJWT);
            String tokenType = decodedJWT.getClaim("type").asString();
            String email = decodedJWT.getSubject();

            // Validar que o token é realmente um refresh token
            if (!"refreshToken".equals(tokenType)) {
                throw new JwtManagerException("Token inválido: não é um refresh token");
            }

            // Recuperar usuário e validar
            UserLoginEntity user = (UserLoginEntity) userRepository.findByEmail(email);

            if (user == null){
                throw new UsernameNotFoundException("Usuário não encontrado");
            }

            // Validar que o ID do usuário no token corresponde ao usuário autenticado
            if (userTokenOf.getUserId().longValue() != user.getId().longValue()) {
                throw new JwtManagerException("Token expirado: usuário não corresponde");
            }

            // Revogar o refresh token antigo
            refreshTokenStore.revoke(tokenId);

            // Gerar novos tokens
            String newAccess = createAccessToken(user);
            String newRefresh = createRefreshToken(user);

            return new TokenResponse(newAccess, newRefresh);

        } catch (JWTVerificationException exception) {
            throw new JwtManagerException("Token expirado ou inválido: " + exception.getMessage());
        }
    }

    public TokenResponse refresh(String refreshTokenJWT) {
        try {
            // Extrair o tokenId do JWT sem validar a assinatura primeiro
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            var verifier = JWT.require(algorithm)
                    .withIssuer("Facial_authI9_API")
                    .build();

            DecodedJWT decodedJWT = verifier.verify(refreshTokenJWT);
            String tokenIdString = decodedJWT.getClaim("tokenId").asString();

            if (tokenIdString == null || tokenIdString.isEmpty()) {
                throw new JwtManagerException("Token inválido: tokenId não encontrado");
            }

            UUID tokenId = UUID.fromString(tokenIdString);
            return refresh(refreshTokenJWT, tokenId);

        } catch (IllegalArgumentException e) {
            throw new JwtManagerException("TokenId inválido no token JWT");
        } catch (JWTVerificationException exception) {
            throw new JwtManagerException("Token expirado ou inválido: " + exception.getMessage());
        }
    }

    public String getSubject(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            return JWT.require(algorithm)
                    .withIssuer("Facial_authI9_API")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception){
            throw new JwtManagerException("Erro ao validar token: " + exception.getMessage());
        }
    }

    public ResponseCookie cookieConfig(String name, String value, int maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true) // Importante: cookies com SameSite=None ou Strict exigem Secure
                .path("/")
                .maxAge(maxAge)
                .sameSite("Strict") // Impede que o cookie seja enviado em requisições de outros sites
                .build();
    }


    public Instant expiresAt(long minutes) {
        return LocalDateTime.now().plusMinutes(minutes).toInstant(ZoneOffset.of("-03:00"));
    }

    public ResponseCookie clearCookie(){
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) // <--- Define o tempo de vida como ZERO para o navegador apagá-lo
                .sameSite("Strict")
                .build();
    }

    public String extractRefreshTokenFromRequest(HttpServletRequest request){
        // 1. Extrair o Refresh Token do Cookie para invalidá-lo
        String refreshToken = null;
         if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        return refreshToken;
    }
}
