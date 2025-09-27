package com.clinicboard.user_service.infrastructure.config.security.filters;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro responsável por autenticar requisições internas vindas do Gateway.
 */
@Component
public class GatewayRequestFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";
    private static final String USER_EMAIL_HEADER = "X-User-Email";
    private static final String USER_NAME_HEADER = "X-User-Name";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Verifica se a requisição vem do Gateway através dos headers X-User-*
        String userId = request.getHeader(USER_ID_HEADER);
        String userRole = request.getHeader(USER_ROLE_HEADER);
        String userEmail = request.getHeader(USER_EMAIL_HEADER);
        String userName = request.getHeader(USER_NAME_HEADER);

        if (isGatewayRequest(userId, userRole, userEmail)) {
            authenticateGatewayRequest(userId, userRole, userEmail, userName);
            System.out.println("🔐 Requisição interna autenticada do Gateway para usuário: " + userEmail);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isGatewayRequest(String userId, String userRole, String userEmail) {
        return userId != null && !userId.trim().isEmpty() &&
                userRole != null && !userRole.trim().isEmpty() &&
                userEmail != null && !userEmail.trim().isEmpty();
    }

    private void authenticateGatewayRequest(String userId, String userRole, String userEmail, String userName) {
        // Converter role para formato Spring Security
        String springRole = ROLE_PREFIX + userRole.toUpperCase();
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(springRole));

        // Criar principal com dados do Gateway
        GatewayUserPrincipal principal = new GatewayUserPrincipal(userId, userEmail, userRole, userName);

        // Criar authentication token
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null,
                authorities);

        // Definir no contexto de segurança
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static class GatewayUserPrincipal {
        private final String userId;
        private final String email;
        private final String role;
        private final String name;

        public GatewayUserPrincipal(String userId, String email, String role, String name) {
            this.userId = userId;
            this.email = email;
            this.role = role;
            this.name = name != null ? name : "Unknown";
        }

        public String getUserId() { return userId; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getName() { return name; }

        @Override
        public String toString() {
            return String.format("GatewayUser{id='%s', email='%s', role='%s'}",
                    userId, email, role);
        }
    }
}