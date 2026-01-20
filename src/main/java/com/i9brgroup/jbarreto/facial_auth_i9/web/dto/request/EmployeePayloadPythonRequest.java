package com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request;

public record EmployeePayloadPythonRequest(
        Long id,
        String name,
        String email,
        String siteId,
        String localId,
        String photoKey
) {
}
