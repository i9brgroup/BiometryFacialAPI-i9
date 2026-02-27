package com.i9brgroup.jbarreto.facial_auth_i9.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.employee.Employee;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.EmployeeService;
import com.i9brgroup.jbarreto.facial_auth_i9.resources.repository.auth.UserRepository;
import com.i9brgroup.jbarreto.facial_auth_i9.resources.repository.employee.EmployeeRepository;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request.EmployeePayloadPythonRequest;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.EmployeeDatasResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.EmployeeSearchResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.ProcessPayloadResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.sdk.aws.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);
    private final HttpClient httpClient;
    @Value("${api.security.token.api_key}")
    private String API_KEY;
    @Value("${api.service.python.base_url}")
    private String BASE_URL_PYTHON;
    private Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, S3Service s3Service, ObjectMapper objectMapper, HttpClient httpClient) {
        this.httpClient = httpClient;
        this.s3Service = s3Service;
        this.employeeRepository = employeeRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Page<EmployeeDatasResponse> buscarTodosFuncionarios(Pageable pagination) {
        Pageable pageable = PageRequest.of(pagination.getPageNumber(), pagination.getPageSize(), Sort.by("firstName"));
        return employeeRepository.findAll(pageable).map(EmployeeDatasResponse::new);
    }

    @Override
    public EmployeeSearchResponse buscarPorIdOuNome(String localId) {
        log.info("Login do usuário {} iniciou a busca por funcionário com localId: {}", auth != null ? auth.getName() : "Desconecido", localId);
        var employee = employeeRepository.findByLocalId(localId);
        if (employee != null){
            var nameNormalized = employee.getName().toLowerCase().replaceAll(" ", "_");
            String extension = ""; // Se não tiver a extensão no banco, ou se for padrão .jpg

            String s3Key = employee.getSiteId() + "_" + employee.getLocalId() + "_" + nameNormalized + extension;
            String presignedUrl = s3Service.getPreSignedUrl(s3Key);

            return new EmployeeSearchResponse(employee, presignedUrl);
        }
        log.error("Funcionário não encontrado com o localId: {} ", localId);
        throw new UsernameNotFoundException("Funcionário não encontrado com o localId: " + localId);
    }

    @Override
    public ProcessPayloadResponse processPayload(EmployeePayloadPythonRequest payload, MultipartFile file) {
        String sendPayloadURL = BASE_URL_PYTHON + "employee/payload";

        if (auth != null) {
            log.info("Usuario {} iniciou o processamento do payload do funcionário: {}", auth.getName(), payload.name());
        }

        if (file.isEmpty()) {
            log.error("Arquivo de foto vazio recebido pela usuario {} para o funcionário {}. ", auth != null ? auth.getName() : "Desconecido", payload.name());
            throw new RuntimeException("File is empty");
        }

        try {
            var originalFileName = file.getOriginalFilename();
            var nameNormalized = payload.name().toLowerCase().replaceAll(" ", "_");
            String extension = "";

            int i = file.getOriginalFilename().lastIndexOf('.');
            if (i > 0) {
                extension = file.getOriginalFilename().substring(i);
            }

            String s3Key = payload.siteId() + "_" + payload.localId() + "_" + nameNormalized + extension;


            var s3Response = s3Service.uploadFile(file, s3Key);

            if (s3Response) {
                log.info("Arquivo {} enviado com sucesso para o S3 com a chave {}", originalFileName, s3Key);
                var presignedURL = s3Service.generatedPreSignedUrlForPhotosEmployees(s3Key);
                payload = new EmployeePayloadPythonRequest(
                        payload.id(),
                        payload.name(),
                        payload.email(),
                        payload.siteId(),
                        payload.localId(),
                        presignedURL
                );

                var paylaodResponse = sendPayloadToPythonService(payload, sendPayloadURL);

                if (paylaodResponse.status().equalsIgnoreCase("done")) {
                    log.info("Payload enviado e processado com sucesso.");
                    return new ProcessPayloadResponse(
                            paylaodResponse.status()
                    );
                } else {
                    log.error("Payload não aceito pelo serviço Python. Status: {}", paylaodResponse.status());
                    throw new RuntimeException("Payload não aceito pelo serviço Python");
                }
            }
        } catch (ClosedChannelException closedChannelException) {
            log.error("Conexão encerrada abruptamente ao processar payload: {}", closedChannelException.getMessage());
            throw new RuntimeException("Erro no processamento do funcionário", closedChannelException);
        } catch (Exception e) {
            log.error("Erro ao processar payload: {}", e.getMessage());
            throw new RuntimeException("Erro no processamento do funcionário", e);
        }
        throw new RuntimeException("Erro desconhecido no processamento do funcionário");
    }

    @Override
    public ProcessPayloadResponse sendPayloadToPythonService(EmployeePayloadPythonRequest payload, String url) throws ClosedChannelException {
        try {
            String jsonBody = objectMapper.writeValueAsString(payload);

            log.debug("DEBUG CONTEUDO DO JSON {}", jsonBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .header("X-API-KEY", API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Erro ao enviar payload para Python. Status: {} Body: {}",
                        response.statusCode(), response.body());
                throw new RuntimeException("Falha na integração com serviço Python: " + response.statusCode());
            }

            log.info("Payload enviado com sucesso para o serviço Python. Status: {}", response.statusCode());

            return objectMapper.readValue(response.body(), ProcessPayloadResponse.class);

        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar payload para JSON: {}", e.getMessage());
            var deleteFile = s3Service.deleteFile(payload.photoKey());
            if (deleteFile) {
                log.info("Rollback realizado: Arquivo {} removido do S3 após erro de serialização. ", payload.photoKey());
            }
            throw new RuntimeException("Erro de serialização do payload", e);
        } catch (ClosedChannelException e) {
            log.error("Conexão encerrada abruptamente. O serviço Python pode estar offline ou reiniciando. ", e);
            var deleteFile = s3Service.deleteFile(payload.photoKey());
            if (deleteFile) {
                log.info("Rollback realizado: Arquivo {} removido do S3 após erro de conexao fechada pelo python. ", payload.photoKey());
            }
            throw new ClosedChannelException();
        } catch (IOException | InterruptedException e) {
            log.error("Erro na comunicação com o serviço Python: {}", e.getMessage());
            var deleteFile = s3Service.deleteFile(payload.photoKey());
            if (deleteFile) {
                log.info("Rollback realizado: Arquivo {} removido do S3 após erro de comunicacao - IOException. ", payload.photoKey());
            }
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro de rede ao comunicar com serviço Python", e);
        }
    }
}
