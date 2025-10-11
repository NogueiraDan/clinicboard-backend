package com.clinicboard.user_service.units.domain;

import com.clinicboard.user_service.domain.User;
import com.clinicboard.user_service.domain.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User - Testes de Domínio")
class UserTest {

    @Test
    @DisplayName("Deve criar usuário com dados válidos")
    void deveCriarUsuarioComDadosValidos() {
        // Given & When
        User user = new User();
        user.setName("João Silva");
        user.setEmail("joao@clinic.com");
        user.setPassword("senha123");
        user.setContact("+55 11999887766");
        user.setRole(UserRole.PROFESSIONAL);

        // Then
        assertNotNull(user);
        assertEquals("João Silva", user.getName());
        assertEquals("joao@clinic.com", user.getEmail());
        assertEquals("senha123", user.getPassword());
        assertEquals("+55 11999887766", user.getContact());
        assertEquals(UserRole.PROFESSIONAL, user.getRole());
        assertEquals("joao@clinic.com", user.getUsername()); // Username deve ser o email
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    @DisplayName("Deve aceitar todos os tipos de role válidos")
    void deveAceitarTodosTiposDeRoleValidos(UserRole role) {
        // Given & When
        User user = new User();
        user.setRole(role);

        // Then
        assertEquals(role, user.getRole());
    }

    @Test
    @DisplayName("Deve definir role padrão como PROFESSIONAL")
    void deveDefinirRolePadrao() {
        // Given & When
        User user = new User();

        // Then
        assertEquals(UserRole.PROFESSIONAL, user.getRole());
    }

    @Test
    @DisplayName("Deve retornar authorities corretas para ADMIN")
    void deveRetornarAuthoritiesCorretasParaAdmin() {
        // Given
        User user = new User();
        user.setRole(UserRole.ADMIN);

        // When
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // Then
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Deve retornar authorities corretas para PROFESSIONAL")
    void deveRetornarAuthoritiesCorretasParaProfessional() {
        // Given
        User user = new User();
        user.setRole(UserRole.PROFESSIONAL);

        // When
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // Then
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_PROFESSIONAL")));
    }

    @Test
    @DisplayName("Deve retornar email como username")
    void deveRetornarEmailComoUsername() {
        // Given
        User user = new User();
        user.setEmail("test@clinic.com");

        // When & Then
        assertEquals("test@clinic.com", user.getUsername());
    }

    @Test
    @DisplayName("Deve implementar equals e hashCode baseado no ID")
    void deveImplementarEqualsEHashCodeBaseadoNoId() {
        // Given
        User user1 = new User();
        user1.setId("user-123");
        user1.setName("João Silva");

        User user2 = new User();
        user2.setId("user-123");
        user2.setName("João Santos"); // Nome diferente

        User user3 = new User();
        user3.setId("user-456");
        user3.setName("João Silva"); // ID diferente

        // Then
        assertEquals(user1, user2); // Mesmo ID
        assertNotEquals(user1, user3); // IDs diferentes
        assertEquals(user1.hashCode(), user2.hashCode()); // Mesmo hashCode para mesmo ID
    }

    @Test
    @DisplayName("Deve implementar UserDetails corretamente")
    void deveImplementarUserDetailsCorretamente() {
        // Given
        User user = new User();
        user.setEmail("test@clinic.com");
        user.setPassword("senha123");
        user.setRole(UserRole.PROFESSIONAL);

        // Then
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
        assertEquals("test@clinic.com", user.getUsername());
        assertEquals("senha123", user.getPassword());
        assertNotNull(user.getAuthorities());
    }
}