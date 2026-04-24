package com.i9brgroup.jbarreto.facial_auth_i9.domain.models.auth;

import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshToken {

    private UUID id;
    private String tokenHash;
    private Long userId;
    private Instant expiresAt;
}
