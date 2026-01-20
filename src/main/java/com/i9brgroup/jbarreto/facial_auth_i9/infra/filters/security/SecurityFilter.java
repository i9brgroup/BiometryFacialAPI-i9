package com.i9brgroup.jbarreto.facial_auth_i9.infra.filters.security;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.UserLoginEntity;
import com.i9brgroup.jbarreto.facial_auth_i9.infra.security.service.TokenService;
import com.i9brgroup.jbarreto.facial_auth_i9.resources.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    private final TokenService tokenService;
    private final UserRepository userRepository;

    public SecurityFilter(TokenService tokenService, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        // Skip the filter for POST /api/v1/auth/login
        return "POST".equalsIgnoreCase(method) && uri != null && uri.endsWith("/api/v1/auth/login");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        var tokenJwt = recoverToken(request);
        if (tokenJwt != null) {
            var subject = tokenService.getSubject(tokenJwt);
            logger.info("JWT token found in request {} {} for subject {}", method, uri, subject);

            var user = userRepository.findByEmail(subject);
            var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            var userPrincipal = (UserLoginEntity) auth.getPrincipal();
            System.out.println("Roles do usuario: " + userPrincipal.getUsername() + " " + userPrincipal.getRoles());
        } else {
            logger.trace("No JWT token found in request {} {}", method, uri);
        }
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token present; return null so caller can decide what to do
            return null;
        }
        return authHeader.substring(7);
    }
}
