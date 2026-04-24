package com.i9brgroup.jbarreto.facial_auth_i9.domain.service;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.auth.UserLoginEntity;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.EmployeeService;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.IAuthenticationFacade;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.ObjetoS3Service;
import com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.aws.S3Service;
import com.i9brgroup.jbarreto.facial_auth_i9.resources.repository.employee.EmployeeRepository;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.EmployeeDatasResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.EmployeeSearchResponse;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final S3Service s3Service;
    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);
    private final ObjetoS3Service objetoS3Service;
    private final IAuthenticationFacade authenticationFacade;

    public EmployeeServiceImpl(IAuthenticationFacade authenticationFacade, EmployeeRepository employeeRepository, S3Service s3Service, ObjetoS3Service objetoS3Service) {
        this.authenticationFacade = authenticationFacade;
        this.s3Service = s3Service;
        this.employeeRepository = employeeRepository;
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
}
