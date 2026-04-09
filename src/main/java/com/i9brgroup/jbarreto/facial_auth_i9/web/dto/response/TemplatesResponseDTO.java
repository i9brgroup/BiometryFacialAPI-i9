package com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.employee.Employee;

import java.util.Set;

public record TemplatesResponseDTO(
        String id,
        String nomeEmployee,
        String badgeNumber,
        String template
) {
    public TemplatesResponseDTO(Employee employee){
        this(employee.getId(),employee.getName(), employee.getBadgeNumber(), employee.getFaceTemplate());
    }
}
