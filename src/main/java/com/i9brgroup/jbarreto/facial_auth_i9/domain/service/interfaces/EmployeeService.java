package com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces;

import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.EmployeeDatasResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.EmployeeSearchResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.ListEmployeePageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {
    Page<ListEmployeePageResponse> getAllEmployeePageable(Integer page, Integer size, String orderBy, String direction);
    EmployeeSearchResponse buscarPorId(String localId);
}
