package com.i9brgroup.jbarreto.facial_auth_i9.domain.models.employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.ParamDef;

@Entity
@Table(name = "EMPLOYEE")
@Immutable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "siteId", type = String.class))
@Filter(name = "tenantFilter", condition = "[EmployeeSiteID] = :siteId")
public class Employee {

    @Column(name = "[ID]")
    @Id
    private String id;
    @Column(name = "[FirstName]")
    private String firstName;
    @Column(name = "[LastName]")
    private String lastName;
    @Column(name = "[EmployeeEMailAddress]")
    private String email;
    @Column(name = "[EmployeeSiteID]")
    private String siteId;
    @Column(name = "[EmployeeLocalID]")
    private String localId;
    @Column(name = "[FingerPrintTemplate]")
    private String faceTemplate;

    public String getName() {
        return (firstName != null ? firstName : "") + (lastName != null ? " " + lastName : "");
    }
}
