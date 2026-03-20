package com.i9brgroup.jbarreto.facial_auth_i9.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.auth.ObjetoS3;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.auth.UserLoginEntity;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.EmployeeService;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.IAuthenticationFacade;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.ObjetoS3Service;
import com.i9brgroup.jbarreto.facial_auth_i9.resources.repository.employee.EmployeeRepository;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request.EmployeePayloadPythonRequest;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.EmployeeDatasResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.EmployeeSearchResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.ProcessPayloadResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.aws.S3Service;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
    private final ObjetoS3Service objetoS3Service;
    private final IAuthenticationFacade authenticationFacade;

    public EmployeeServiceImpl(IAuthenticationFacade authenticationFacade, EmployeeRepository employeeRepository, S3Service s3Service, ObjectMapper objectMapper, HttpClient httpClient, ObjetoS3Service objetoS3Service) {
        this.authenticationFacade = authenticationFacade;
        this.httpClient = httpClient;
        this.s3Service = s3Service;
        this.employeeRepository = employeeRepository;
        this.objectMapper = objectMapper;
        this.objetoS3Service = objetoS3Service;
    }

    @Override
    public Page<EmployeeDatasResponse> buscarTodosFuncionarios(Pageable pagination) {
        Pageable pageable = PageRequest.of(pagination.getPageNumber(), pagination.getPageSize(), Sort.by("firstName"));
        return employeeRepository.findAll(pageable).map(EmployeeDatasResponse::new);
    }

    @Override
    public EmployeeSearchResponse buscarPorId(String id) {
        UserLoginEntity auth = authenticationFacade.getAuthentication();
        log.info("Login do usuário {} iniciou a busca por funcionário com localId: {}", auth != null ? auth.getUsername() : "Desconhecido", id);
        var employee = employeeRepository.findEmployeeById(id, auth.getSiteId());

        if (employee == null) {
            log.error("Funcionário não encontrado com o localId: {} ", id);
            throw new EntityNotFoundException("Funcionário não encontrado com o localId: " + id);
        }

        var s3Key = objetoS3Service.findById(employee.getId());
        String presignedUrl = null;

        if (s3Key != null) {
            log.debug("Gerando URL pré-assinada para chave S3: {}", s3Key);
            presignedUrl = s3Service.getPreSignedUrl(s3Key);
        } else {
            log.warn("Nenhum objeto S3 encontrado para o funcionário com localId: {}. O funcionário será retornado sem URL pré-assinada.", id);
        }

        return new EmployeeSearchResponse(employee, presignedUrl);
    }

    protected String criaS3Key(String nome, String siteId, String localId, MultipartFile file) {
        var nameNormalized = nome.toLowerCase().replaceAll(" ", "_");
        String originalName = file.getOriginalFilename();

        // Se o nome original contém '?', corta tudo que vem depois (limpa parâmetros de URL)
        if (originalName != null && originalName.contains("?")) {
            originalName = originalName.substring(0, originalName.indexOf("?"));
        }

        String extension = "";
        if (originalName != null) {
            int i = originalName.lastIndexOf('.');
            if (i > 0) {
                extension = originalName.substring(i);
            }
        }

        return siteId + "_" + localId + "_" + nameNormalized + extension;
    }

    @Override
    public ProcessPayloadResponse processPayload(EmployeePayloadPythonRequest payload, MultipartFile file) {
        String sendPayloadURL = BASE_URL_PYTHON + "employee/payload";
        UserLoginEntity auth = authenticationFacade.getAuthentication();
        String s3Key = null;

        if (auth != null) {
            log.info("Usuario {} iniciou o processamento do payload do funcionário: {}", auth.getUsername(), payload.name());
        }

        if (file.isEmpty()) {
            log.error("Arquivo de foto vazio recebido pela usuario {} para o funcionário {}. ", auth != null ? auth.getUsername() : "Desconhecido", payload.name());
            throw new RuntimeException("File is empty");
        }

        var employee = employeeRepository.findEmployeeById(payload.id(), auth.getSiteId());

        if (employee == null) {
            log.error("Funcionário não encontrado com o id: {} para processar payload.", payload.id());
            throw new EntityNotFoundException("Funcionário não encontrado com o localId: " + payload.id());
        }

        try {
            s3Key = criaS3Key(payload.name(), payload.siteId(), payload.localId(), file);

            var s3Response = s3Service.uploadFile(file, s3Key);
            // Nesse ponto salvamos o nome da foto e a extensao juntamente com o ID do usuario.

            if (s3Response) {
                log.error("VALOR DA CHAVE DO S3 PARA DEBUGAR {}", s3Key);
                var objetoS3 = new ObjetoS3(employee.getId(), s3Key);
                var objetoS3Salvo = objetoS3Service.save(objetoS3);

                if (objetoS3Salvo != null) {
                    log.info("Arquivo enviado com sucesso para o S3 com a chave {}", objetoS3Salvo.getNomeArquivoS3());
                    var presignedURL = s3Service.generatedPreSignedUrlForPhotosEmployees(objetoS3Salvo.getNomeArquivoS3());
                    payload = new EmployeePayloadPythonRequest(
                            employee.getId(),
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
                    }
                }
            }

        } catch (Exception e) {
            if (s3Key != null) {
                log.error("Iniciando Rollback do S3 para a chave: {}", s3Key);
                s3Service.executaRollback(s3Key);
            }
            throw new RuntimeException("Erro no processamento do funcionário", e);
        }
        throw new RuntimeException("Erro no processamento do funcionário: Falha ao enviar o payload para o serviço Python");
    }

    @Override
    public ProcessPayloadResponse sendPayloadToPythonService(EmployeePayloadPythonRequest payload, String url) {
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
                var keyPhotoDatabase = objetoS3Service.findById(payload.id());
                var deleteFile = s3Service.deleteFile(payload.photoKey());

                if (keyPhotoDatabase != null && deleteFile) {
                    log.info("Rollback realizado: Arquivo {} removido do S3 após resposta de erro do python. ", payload.photoKey());
                }
                throw new RuntimeException("Falha na integração com serviço Python: " + response.statusCode());
            }

            log.info("Payload enviado com sucesso para o serviço Python. Status: {}", response.statusCode());

            return objectMapper.readValue(response.body(), ProcessPayloadResponse.class);

        } catch (Exception e) {
            log.error("Erro ao enviar payload para o serviço python: {}", e.getMessage());
            throw new RuntimeException("Erro ao enviar payload para serviço python: ", e);
        }
    }
}
