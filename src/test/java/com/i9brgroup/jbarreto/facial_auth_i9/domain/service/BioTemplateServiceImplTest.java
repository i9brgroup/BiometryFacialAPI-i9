package com.i9brgroup.jbarreto.facial_auth_i9.domain.service;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.employee.Employee;
import com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.exceptions.model.TemplatesNotFoundException;
import com.i9brgroup.jbarreto.facial_auth_i9.resources.repository.employee.EmployeeRepository;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.TemplatesResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BioTemplateServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private BioTemplateServiceImpl bioTemplateService;

    @Test
    @DisplayName("Deve retornar um set de templates quando encontrados no banco")
    void deveRetornarTemplatesQuandoEncontrados() throws TemplatesNotFoundException {
        // ARRANGE
        String siteId = "SITE01";
        Employee employee1 = new Employee("1", "João", "template1");
        employee1.setBadgeNumber("123");
        Employee employee2 = new Employee("2", "Maria", "template2");
        employee2.setBadgeNumber("456");

        Set<Employee> templates = Set.of(employee1, employee2);
        given(employeeRepository.findTemplatesBySiteId(siteId)).willReturn(templates);

        // ACT
        Set<TemplatesResponseDTO> response = bioTemplateService.getTemplatesOnDatabase(siteId);

        // ASSERT
        assertNotNull(response);
        assertEquals(2, response.size());
        verify(employeeRepository).findTemplatesBySiteId(siteId);
    }

    @Test
    @DisplayName("Deve lancar TemplatesNotFoundException quando nenhum template for encontrado")
    void deveLancarExceptionQuandoNenhumTemplateEncontrado() {
        // ARRANGE
        String siteId = "SITE_VAZIO";
        given(employeeRepository.findTemplatesBySiteId(siteId)).willReturn(Collections.emptySet());

        // ACT & ASSERT
        TemplatesNotFoundException exception = assertThrows(TemplatesNotFoundException.class, () -> {
            bioTemplateService.getTemplatesOnDatabase(siteId);
        });

        assertEquals("Nenhum template carregado do banco para o siteID: " + siteId, exception.getMessage());
        verify(employeeRepository).findTemplatesBySiteId(siteId);
    }
}