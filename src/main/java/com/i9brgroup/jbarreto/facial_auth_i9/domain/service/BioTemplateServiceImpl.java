package com.i9brgroup.jbarreto.facial_auth_i9.domain.service;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.employee.Employee;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.IBioTemplatesService;
import com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.exceptions.model.TemplatesNotFoundException;
import com.i9brgroup.jbarreto.facial_auth_i9.resources.repository.employee.EmployeeRepository;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.TemplatesResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class BioTemplateServiceImpl implements IBioTemplatesService {
    private final EmployeeRepository employeeRepository;
    private final Logger logger = LoggerFactory.getLogger(BioTemplateServiceImpl.class);

    public BioTemplateServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Set<TemplatesResponseDTO> getTemplatesOnDatabase(String siteId) throws TemplatesNotFoundException {
        var templates = employeeRepository.findTemplatesBySiteId(siteId);
        Set<TemplatesResponseDTO> templatesDTO = new HashSet<>();
        if (templates.isEmpty()){
            throw new TemplatesNotFoundException("Nenhum template carregado do banco para o siteID: " + siteId);
        } else {
            for (Employee employee : templates) {
                templatesDTO.add(new TemplatesResponseDTO(employee));
            }
            logger.info("Templates carregados com sucesso para o siteID: {}", siteId);
            logger.info("Número de templates carregados: {}", templatesDTO.size());
        }
        return templatesDTO;
    }
}
