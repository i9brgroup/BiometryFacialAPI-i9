package com.i9brgroup.jbarreto.facial_auth_i9.web.controller;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.EmployeeServiceImpl;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.EmployeeDatasResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.sdk.aws.S3Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/api/v1/employees")
@RestController
public class EmployeeController {

    private final EmployeeServiceImpl employeeService;
    private final S3Service s3Service;

    public EmployeeController(EmployeeServiceImpl employeeService, S3Service s3Service) {
        this.s3Service = s3Service;
        this.employeeService = employeeService;
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

    @PostMapping(value = "/send-image-s3", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<?> sendImageToS3(@RequestParam("file") MultipartFile file){
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        var image = s3Service.uploadFile(file);
        if (image){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
}
