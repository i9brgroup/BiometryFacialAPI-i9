package com.i9brgroup.jbarreto.facial_auth_i9.infra.filters.aspect;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.UserLoginEntity;
import jakarta.persistence.EntityManager;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Aspect
@Configuration
public class UserFilterAspect {

    private final EntityManager entityManager;

    public UserFilterAspect(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Before(value = ("execution( * com.i9brgroup.jbarreto.facial_auth_i9.resources.repository.*.*(..))"))
    public void setUserFilter() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 1. SE NÃO ESTIVER LOGADO (Login, Register, Swagger)
        // O auth.getPrincipal() será a string "anonymousUser"
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            // Não faz nada! Deixa a query passar sem filtro.
            // Isso permite que o userRepository.findByEmail funcione.
            return;
        }

        // 2. SE ESTIVER LOGADO (JWT Validado)
        // Agora é seguro fazer o Cast
        if (auth.getPrincipal() instanceof UserLoginEntity) {
            UserLoginEntity user = (UserLoginEntity) auth.getPrincipal();
            Session session = entityManager.unwrap(Session.class);
            System.out.println("Site ID: " + user.getSiteId());
            session.enableFilter("tenantFilter").setParameter("siteId", user.getSiteId());
        }
    }
}
