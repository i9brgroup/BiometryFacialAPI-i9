package com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.Employee;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.EmployeeDatasResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeService {
    Page<EmployeeDatasResponse> buscarTodosFuncionarios(Pageable pageable);
    List<EmployeeDatasResponse> buscarPorIdOuNome(String searchTerm);
}
