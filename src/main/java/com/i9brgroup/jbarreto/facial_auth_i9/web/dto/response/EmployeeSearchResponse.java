package com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.employee.Employee;
 
public record EmployeeSearchResponse(
        String id,
        String name,
        String email,
        String siteId,
        String localId,
        String faceTemplate,
        String urlPhoto
) {
    public EmployeeSearchResponse(Employee employee, String urlPhoto) {
        this(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getSiteId(),
                employee.getLocalId(),
                employee.getFaceTemplate(),
                urlPhoto
        );
    }
}
