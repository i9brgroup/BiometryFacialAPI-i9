package com.i9brgroup.jbarreto.facial_auth_i9.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.EmployeeService;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request.EmployeePayloadPythonRequest;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.EmployeeDatasResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/api/v1/employees")
@RestController
public class EmployeeController {

    private final EmployeeService employeeService;
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(EmployeeController.class);


    public EmployeeController(EmployeeService employeeService, com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.employeeService = employeeService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/health")
    public String checkHealth() {
        return "API Esta funcionando";
    }

    @GetMapping("/list-employees")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<EmployeeDatasResponse>> listEmployees(@PageableDefault (size = 10, sort = "name") Pageable pagination) {
        var page = employeeService.buscarTodosFuncionarios(pagination);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/search-employees/{searchTerm}")
    public ResponseEntity<List<EmployeeDatasResponse>> searchEmployees(@PathVariable String searchTerm) {
        var employees = employeeService.buscarPorIdOuNome(searchTerm);
        return ResponseEntity.ok(employees);
    }

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
            var response = employeeService.processPayload(payload, file);

            if (response.status().equalsIgnoreCase("success")) {
                return ResponseEntity.ok().body(response);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Erro ao processar o JSON do payload: " + e.getMessage());
        }
    }
}
