package com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces;

import com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.exceptions.model.TemplatesNotFoundException;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.TemplatesResponseDTO;

import java.util.Set;

public interface IBioTemplatesService {
    Set<TemplatesResponseDTO> getTemplatesOnDatabase(String siteId) throws TemplatesNotFoundException;
}
