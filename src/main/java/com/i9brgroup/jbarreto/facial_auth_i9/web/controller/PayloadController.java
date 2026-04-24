package com.i9brgroup.jbarreto.facial_auth_i9.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.PayloadService;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request.EmployeePayloadPythonRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/payloads")
@RequiredArgsConstructor
public class PayloadController {

    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(PayloadController.class);
    private final PayloadService payloadService;

    @PostMapping(value = "/process-payload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<?> sendImageToS3AndProcessEmbeddedTemplate(
            @RequestParam("file") MultipartFile file,
            @RequestPart("payload") String payloadJson) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        try {
            EmployeePayloadPythonRequest payload = objectMapper.readValue(payloadJson, EmployeePayloadPythonRequest.class);

            log.info("Received payload: {}", payload);
            var response = payloadService.processPayload(payload, file);

            if (response.status().equalsIgnoreCase("done")) {
                return ResponseEntity.ok().body(response);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Erro ao processar o JSON do payload: " + e.getMessage());
        }
    }
}
