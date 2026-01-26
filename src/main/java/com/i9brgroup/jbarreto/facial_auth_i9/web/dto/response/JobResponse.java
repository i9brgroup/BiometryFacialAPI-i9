package com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record JobResponse(
        String status,
        @JsonProperty("created_at")
        LocalDateTime createdAt,
        @JsonProperty("started_at")
        LocalDateTime startedAt,
        @JsonProperty("finished_at")
        LocalDateTime finishedAt,
        String result,
        String error
) {
}
