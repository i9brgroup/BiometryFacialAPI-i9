package com.i9brgroup.jbarreto.facial_auth_i9.domain.service;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.Employee;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.EmployeeService;
import com.i9brgroup.jbarreto.facial_auth_i9.resources.repository.EmployeeRepository;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.EmployeeDatasResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
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
                .map(EmployeeDatasResponse::new)
                .toList();
    }
}
