package com.i9brgroup.jbarreto.facial_auth_i9.resources.repository.employee;
 
import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.employee.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.util.List;
 
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    List<Employee> findByNameContainingIgnoreCase(String name);

    @Query("SELECT e FROM Employee e WHERE e.id = :searchTerm OR LOWER(e.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Employee> searchByIdOrName(@Param("searchTerm") String searchTerm);
}
