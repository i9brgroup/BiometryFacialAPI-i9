package com.i9brgroup.jbarreto.facial_auth_i9.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.ParamDef;

@Entity
@Table(name = "EMPLOYEE")
@Immutable
@Getter
@AllArgsConstructor
@NoArgsConstructor
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "siteId", type = String.class))
@Filter(name = "tenantFilter", condition = "EmployeeSiteID = :siteId")
public class Employee {

    @Column(name = "[ID]")
    @Id
    String id;
    @Column(name = "[Name]")
    String name;
    @Column(name = "[EmployeeEMailAddress]")
    String email;
    @Column(name = "[EmployeeSiteID]")
    String siteId;
    @Column(name = "[EmployeeLocalID]")
    String localId;
    @Column(name = "faceTemplate")
    String faceTemplate;
}
