package com.i9brgroup.jbarreto.facial_auth_i9.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.EmployeeService;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request.EmployeePayloadPythonRequest;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.EmployeeDatasResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.ProcessPayloadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "bearer")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(EmployeeController.class);

    public EmployeeController(EmployeeService employeeService, ObjectMapper objectMapper) {
        this.employeeService = employeeService;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "Verifica se a API está funcionando", description = "Retorna uma mensagem indicando que a API está funcionando")
    @ApiResponse(description = "Retorna uma mensagem indicando que a API está funcionando",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    @GetMapping("/health")
    public String checkHealth() {
        return "API Esta funcionando";
    }

    @Operation(summary = "Lista todos os funcionários", description = "Retorna uma página com os dados dos funcionários")
    @ApiResponse(responseCode = "200", description = "Retorna uma página com os dados dos funcionários",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = EmployeeDatasResponse.class))))
    @ApiResponse(responseCode = "200", description = "Nenhum funcionário encontrado",
    content = @Content(array = @ArraySchema(schema =  @Schema(implementation = Page.class))))
    @GetMapping("/list-employees")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<EmployeeDatasResponse>> listEmployees(@PageableDefault (size = 10, sort = "name") Pageable pagination) {
        var page = employeeService.buscarTodosFuncionarios(pagination);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Busca funcionários por ID ou nome", description = "Retorna uma lista de funcionários que correspondem ao termo de busca")
    @ApiResponse(responseCode = "200", description = "Retorna uma lista de funcionários que correspondem ao termo de busca",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = EmployeeDatasResponse.class))))
    @ApiResponse(responseCode = "404", description = "Nenhum funcionário encontrado")
    @GetMapping("/search-employees/{searchTerm}")
    public ResponseEntity<List<EmployeeDatasResponse>> searchEmployees(@PathVariable String searchTerm) {
        var employees = employeeService.buscarPorIdOuNome(searchTerm);
        if (!employees.isEmpty()) {
            return ResponseEntity.ok(employees);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(employees);
    }

    @Operation(summary = "Envia imagem para o S3 e processa payload", description = "Recebe dois dados, um arquivo de imagem e um payload em formato JSON, envia a imagem para o S3 e envia o payload para processamento (API Python). O payload contem a URL pronta e pre-assinada para o Python baixar e gerar o embedding.")
    @ApiResponse(responseCode = "200", description = "Payload processado com sucesso",
    content = @Content(schema = @Schema(implementation = ProcessPayloadResponse.class)))
    @ApiResponse(responseCode = "400", description = "Requisição inválida, como arquivo vazio ou JSON mal formatado")
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

            if (response.status().equalsIgnoreCase("done")) {
                return ResponseEntity.ok().body(response);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Erro ao processar o JSON do payload: " + e.getMessage());
        }
    }
}
