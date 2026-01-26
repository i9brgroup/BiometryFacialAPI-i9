package com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessPayloadResponse(
        String status,
        @JsonProperty("job_id")
        String jobID
) {
}
