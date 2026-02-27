package com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces;

import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request.EmployeePayloadPythonRequest;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.EmployeeDatasResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.EmployeeSearchResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.ProcessPayloadResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.StatusJobResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.nio.channels.ClosedChannelException;
import java.util.List;

public interface EmployeeService {
    Page<EmployeeDatasResponse> buscarTodosFuncionarios(Pageable pageable);
    EmployeeSearchResponse buscarPorIdOuNome(String localId);
    ProcessPayloadResponse processPayload(EmployeePayloadPythonRequest payload, MultipartFile file);
    ProcessPayloadResponse sendPayloadToPythonService(EmployeePayloadPythonRequest payload, String url) throws ClosedChannelException;
}
