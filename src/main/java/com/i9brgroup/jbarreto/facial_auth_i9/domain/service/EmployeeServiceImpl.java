package com.i9brgroup.jbarreto.facial_auth_i9.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.Employee;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.EmployeeService;
import com.i9brgroup.jbarreto.facial_auth_i9.resources.repository.EmployeeRepository;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request.EmployeePayloadPythonRequest;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.EmployeeDatasResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.ProcessPayloadResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.StatusJobResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.sdk.aws.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, S3Service s3Service, ObjectMapper objectMapper) {
        this.s3Service = s3Service;
        this.employeeRepository = employeeRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Page<EmployeeDatasResponse> buscarTodosFuncionarios(Pageable pagination) {
        Pageable pageable = PageRequest.of(pagination.getPageNumber(), pagination.getPageSize(), Sort.by("name"));
        return employeeRepository.findAll(pageable).map(EmployeeDatasResponse::new);
    }

    @Override
    public List<EmployeeDatasResponse> buscarPorIdOuNome(String searchTerm) {
        List<Employee> employees = employeeRepository.searchByIdOrName(searchTerm);
        return employees.stream()
                .peek(employee -> {
                    if (employee.getKeyPhoto() != null) {
                        String presignedUrl = s3Service.getPreSignedUrl(employee.getKeyPhoto());
                        employee.setKeyPhoto(presignedUrl);
                    }
                })
                .map(EmployeeDatasResponse::new)
                .toList();
    }

    @Override
    public StatusJobResponse processPayload(EmployeePayloadPythonRequest payload, MultipartFile file) {
        String sendPayloadURL = "http://127.0.0.1:8000/employee/payload";
        String workerStatusJobURL = "http://127.0.0.1:8000/employee/payload/status";

        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        try {
            var originalFileName = file.getOriginalFilename();
            var nameNormalized = payload.name().toLowerCase().replaceAll(" ", "_");
            originalFileName = payload.siteId() + "_" + payload.localId() + "_" + nameNormalized;

            payload = new EmployeePayloadPythonRequest(
                    payload.id(),
                    payload.name(),
                    payload.email(),
                    payload.siteId(),
                    payload.localId(),
                    originalFileName
            );

            var s3Response = s3Service.uploadFile(file, payload.photoKey());

            if (s3Response){
                var paylaodResponse = sendPayloadToPythonService(payload, sendPayloadURL);
                log.info(paylaodResponse.jobID());

                if (paylaodResponse.status().equalsIgnoreCase("accepted")){
                    Thread.sleep(3000);
                    return waitForJobCompletion(paylaodResponse.jobID(), workerStatusJobURL, originalFileName);
                } else {
                    log.error("Payload não aceito pelo serviço Python. Status: {}", paylaodResponse.status());
                    throw new RuntimeException("Payload não aceito pelo serviço Python");
                }
            }
        } catch (Exception e) {
            log.error("Erro ao processar payload: {}", e.getMessage());
            throw new RuntimeException("Erro no processamento do funcionário", e);
        }

        throw new RuntimeException("Erro desconhecido no processamento do funcionário");
    }

    @Override
    public ProcessPayloadResponse sendPayloadToPythonService(EmployeePayloadPythonRequest payload, String url) {
        try {
            String jsonBody = objectMapper.writeValueAsString(payload);

            log.info("DEBUG CONTEUDO DO JSON {}", jsonBody);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Erro ao enviar payload para Python. Status: {} Body: {}",
                        response.statusCode(), response.body());
                throw new RuntimeException("Falha na integração com serviço Python: " + response.statusCode());
            }

            log.info("Payload enviado com sucesso para o serviço Python. Status: {}", response.statusCode());

            return objectMapper.readValue(response.body(), ProcessPayloadResponse.class);

        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar payload para JSON: {}", e.getMessage());
            throw new RuntimeException("Erro de serialização do payload", e);
        } catch (IOException | InterruptedException e) {
            log.error("Erro na comunicação com o serviço Python: {}", e.getMessage());
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro de rede ao comunicar com serviço Python", e);
        }
    }

    @Override
    public StatusJobResponse viweWorkerProcessStatus(String idProcess, String url) {
        String completeUrl = url + "/" + idProcess;
        try {

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(completeUrl))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return objectMapper.readValue(response.body(), StatusJobResponse.class);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public StatusJobResponse waitForJobCompletion(String idProcess, String baseUrl, String keyPhoto) {
        int attempts = 0;
        int maxAttempts = 10;
        int delayBetweenAttemptsMs = 1000;

        while (attempts < maxAttempts) {
            var response = viweWorkerProcessStatus(idProcess, baseUrl);

            if ("done".equalsIgnoreCase(response.job().status())){
                return response;
            }

            if ("failed".equalsIgnoreCase(response.job().status()) || "error".equalsIgnoreCase(response.job().status())) {
                log.warn("Job {} falhou no serviço Python.", idProcess);
                s3Service.deleteFile(keyPhoto);
                throw new RuntimeException("Job " + idProcess + " falhou no serviço Python.");
            }

            if ("pending".equalsIgnoreCase(response.job().status())) {
                log.info("Job {} ainda está pendente. Tentativa {}/{}", idProcess, attempts + 1, maxAttempts);
            }

            log.info("Job {} ainda processando... Tentativa {}/{}", idProcess, attempts + 1, maxAttempts);
            try {
                Thread.sleep(delayBetweenAttemptsMs);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            attempts++;
        }
        log.error("Tempo esgotado aguardando a conclusão do job {}.", idProcess);
        s3Service.deleteFile(keyPhoto);
        throw new RuntimeException("Tempo esgotado aguardando a conclusão do job " + idProcess + ".");
    }
}
