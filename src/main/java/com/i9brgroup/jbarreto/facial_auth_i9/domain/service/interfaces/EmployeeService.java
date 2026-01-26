package com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces;

import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request.EmployeePayloadPythonRequest;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.EmployeeDatasResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.ProcessPayloadResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.StatusJobResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EmployeeService {
    Page<EmployeeDatasResponse> buscarTodosFuncionarios(Pageable pageable);
    List<EmployeeDatasResponse> buscarPorIdOuNome(String searchTerm);
    StatusJobResponse processPayload(EmployeePayloadPythonRequest payload, MultipartFile file);
    ProcessPayloadResponse sendPayloadToPythonService(EmployeePayloadPythonRequest payload, String url);
    StatusJobResponse viweWorkerProcessStatus(String idProcess, String url);
    StatusJobResponse waitForJobCompletion(String idProcess, String url, String keyPhoto);
}
