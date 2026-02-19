package com.i9brgroup.jbarreto.facial_auth_i9.infra.filters.interceptor;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.auth.UserLoginEntity;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class TenantFilterInterceptor implements StatementInspector {

    @Override
    public String inspect(String sql) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            System.out.println("=== USUARIO NAO AUTENTICADO ===");
            return sql;
        }

        if (auth.getPrincipal() instanceof UserLoginEntity) {
            UserLoginEntity user = (UserLoginEntity) auth.getPrincipal();
            String siteId = user.getSiteId();

            System.out.println("=== INTERCEPTANDO SQL ===");
            System.out.println("SQL Original: " + sql);

            // Adiciona filtro WHERE se for uma query na tabela EMPLOYEE
            if (sql.toUpperCase().contains("FROM EMPLOYEE") && !sql.toUpperCase().contains("EMPLOYEESITEID")) {
                String filteredSql = sql.replace("from EMPLOYEE", "from EMPLOYEE where EmployeeSiteID = '" + siteId + "'")
                                        .replace("FROM EMPLOYEE", "FROM EMPLOYEE WHERE EmployeeSiteID = '" + siteId + "'");
                System.out.println("SQL Filtrado: " + filteredSql);
                return filteredSql;
            }
        }

        return sql;
    }
}
