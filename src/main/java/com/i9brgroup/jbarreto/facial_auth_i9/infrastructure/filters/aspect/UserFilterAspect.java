package com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.filters.aspect;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.auth.UserLoginEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Aspect
@Component
public class UserFilterAspect {

    @PersistenceContext(unitName = "employee")
    private EntityManager entityManager;

    @Before("execution(* com.i9brgroup.jbarreto.facial_auth_i9.resources.repository.employee..*.*(..))")
    public void setUserFilter() {
        System.out.println("=== ASPECT EXECUTADO ===");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 1. SE NÃO ESTIVER LOGADO (Login, Register, Swagger)
        // O auth.getPrincipal() será a string "anonymousUser"
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            System.out.println("=== USUARIO NAO AUTENTICADO, PULANDO FILTRO ===");
            // Não faz nada! Deixa a query passar sem filtro.
            // Isso permite que o userRepository.findByEmail funcione.
            return;
        }

        // 2. SE ESTIVER LOGADO (JWT Validado)
        // Agora é seguro fazer o Cast
        if (auth.getPrincipal() instanceof UserLoginEntity) {
            UserLoginEntity user = (UserLoginEntity) auth.getPrincipal();
            System.out.println("=== APLICANDO FILTRO PARA SITE ID: " + user.getSiteId() + " ===");
            
            try {
                Session session = entityManager.unwrap(Session.class);
                session.enableFilter("tenantFilter").setParameter("siteId", user.getSiteId());
                System.out.println("=== FILTRO APLICADO COM SUCESSO NO ENTITY MANAGER: " + entityManager + " PARA O SITE: " + user.getSiteId() + " ===");
                // Forçar o flush ou verificar se a sessão está aberta
                if (!session.isOpen()) {
                    System.err.println("=== SESSÃO DO HIBERNATE FECHADA NO ASPECT! ===");
                }
            } catch (Exception e) {
                System.err.println("=== ERRO AO APLICAR FILTRO ASPECT: " + e.getMessage() + " ===");
                e.printStackTrace();
            }
        }
    }
}
