package com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.Employee;
import jakarta.persistence.Column;

public record EmployeeDatasResponse(
        @Column(name = "ID")
        String id,
        @Column(name = "Name")
        String name,
        @Column(name = "EmployeeEMailAddress")
        String email,
        @Column(name = "EmployeeSiteID")
        String siteId,
        @Column(name = "EmployeeLocalID")
        String localId,
        String faceTemplate
) {
    public EmployeeDatasResponse(Employee employee) {
        this(employee.getId(), employee.getName(), employee.getEmail(), employee.getSiteId(), employee.getLocalId(), employee.getFaceTemplate());
    }
}
