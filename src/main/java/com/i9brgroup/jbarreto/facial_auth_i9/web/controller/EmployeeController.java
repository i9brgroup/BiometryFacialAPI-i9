package com.i9brgroup.jbarreto.facial_auth_i9.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.PayloadService;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.EmployeeService;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.EmployeeDatasResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.EmployeeSearchResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/employees")
@RestController
public class EmployeeController {

    private final EmployeeService employeeService;
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(EmployeeController.class);
    private final PayloadService payloadService;

    public EmployeeController(EmployeeService employeeService, ObjectMapper objectMapper, PayloadService payloadService) {
        this.employeeService = employeeService;
        this.payloadService = payloadService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/health")
    public String checkHealth(HttpServletRequest request) {
        return "API Esta funcionando. IP Real do cliente: " + request.getRemoteAddr();
    }

    @GetMapping("/list-employees")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<EmployeeDatasResponse>> listEmployees(@PageableDefault (size = 10, sort = "firstName") Pageable pagination) {
        var page = employeeService.buscarTodosFuncionarios(pagination);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/search-employees/{id}")
    public ResponseEntity<EmployeeSearchResponse> searchEmployees(
            @PathVariable String id) {
        var employees = employeeService.buscarPorId(id);
        return ResponseEntity.ok(employees);
    }
}
