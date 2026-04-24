package com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.employee.Employee;

public record ListEmployeePageResponse(
        String id,
        String completeName,
        String siteId,
        String badgeNumber,
        boolean active,
        String biometricHash
) {
    public ListEmployeePageResponse(Employee employee) {
        this(employee.getId(), employee.getName(), employee.getSiteId(), employee.getBadgeNumber(), employee.isActive(), employee.getFaceTemplate());
    }
}
