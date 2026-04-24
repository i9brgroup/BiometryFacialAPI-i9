package com.i9brgroup.jbarreto.facial_auth_i9.web.controller;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.EmployeeService;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.EmployeeSearchResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.ListEmployeePageResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/employees")
@RestController
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/health")
    public String checkHealth(HttpServletRequest request) {
        return "API Esta funcionando. IP Real do cliente: " + request.getRemoteAddr();
    }

    @GetMapping("/list-employees")
    public ResponseEntity<Page<ListEmployeePageResponse>> listEmployees(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String orderBy,
            @RequestParam(defaultValue = "ASC") String direction
    ) {
        var employeePageable = employeeService.getAllEmployeePageable(page, size, orderBy, direction);
        return ResponseEntity.ok().body(employeePageable);
    }

    @GetMapping("/search-employees/{id}")
    public ResponseEntity<EmployeeSearchResponse> searchEmployees(
            @PathVariable String id) {
        var employees = employeeService.buscarPorId(id);
        return ResponseEntity.ok(employees);
    }
}
