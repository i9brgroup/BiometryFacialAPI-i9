package com.i9brgroup.jbarreto.facial_auth_i9.resources.repository.employee;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.employee.Employee;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.EmployeeSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
 
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    @Query("SELECT e FROM Employee e WHERE e.id = :searchTerm OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Employee> findByFirstNameOrLastNameContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    @Query("SELECT e FROM Employee e WHERE e.localId = :searchTerm OR LOWER(e.firstName) LIKE LOWER(CONCAT( :searchTerm, '%'))")
    Page<Employee> searchByIdOrName(@Param("searchTerm") String searchTerm, Pageable pageable);
    @Query("SELECT e FROM Employee e WHERE e.id = :localId AND e.siteId = :siteId")
    Employee findEmployeeById(@Param("localId") String id, String siteId);
}
