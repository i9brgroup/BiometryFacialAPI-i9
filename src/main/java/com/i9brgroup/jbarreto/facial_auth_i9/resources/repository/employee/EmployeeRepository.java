package com.i9brgroup.jbarreto.facial_auth_i9.resources.repository.employee;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.employee.Employee;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.EmployeeSearchResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.TemplatesResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface EmployeeRepository extends JpaRepository<Employee, String> {
    @Query("SELECT e FROM Employee e WHERE e.id = :id AND e.siteId = :siteId")
    Employee findEmployeeById(@Param("id") String id, String siteId);

    @Query("SELECT e FROM Employee e WHERE e.siteId = :siteId AND LENGTH(e.faceTemplate) > 650")
    Set<Employee> findTemplatesBySiteId(@Param("siteId") String siteId);
}
