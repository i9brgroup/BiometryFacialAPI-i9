package com.i9brgroup.jbarreto.facial_auth_i9.web.controller;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.IBioTemplatesService;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.TemplatesResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/templates")
public class TemplateController {
    private final IBioTemplatesService bioTemplateService;

    public TemplateController(IBioTemplatesService bioTemplateService) {
        this.bioTemplateService = bioTemplateService;
    }

    @GetMapping("/get-all/{siteId}")
    public ResponseEntity<Set<TemplatesResponseDTO>> getTemplates(@PathVariable String siteId) {
        Set<TemplatesResponseDTO> templates = bioTemplateService.getTemplatesOnDatabase(siteId);
        return ResponseEntity.ok().body(templates);
    }
}
