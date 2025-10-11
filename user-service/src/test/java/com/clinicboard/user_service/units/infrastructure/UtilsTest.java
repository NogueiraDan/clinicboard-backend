package com.clinicboard.user_service.units.infrastructure;

import com.clinicboard.user_service.application.dto.UserResponseDto;
import com.clinicboard.user_service.domain.User;
import com.clinicboard.user_service.domain.UserRole;
import com.clinicboard.user_service.infrastructure.adapters.helpers.Utils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Utils - Testes Unitários")
class UtilsTest {

    @Test
    @DisplayName("Deve converter User para UserResponseDto corretamente")
    void deveConverterUserParaUserResponseDtoCorretamente() {
        // Given
        User user = new User();
        user.setId("user-123");
        user.setName("João Silva");
        user.setEmail("joao@clinic.com");
        user.setContact("+55 11999887766");
        user.setRole(UserRole.PROFESSIONAL);

        // When
        UserResponseDto result = Utils.convertToUserResponseDto(user);

        // Then
        assertNotNull(result);
        assertEquals("user-123", result.getId());
        assertEquals("João Silva", result.getName());
        assertEquals("joao@clinic.com", result.getEmail());
        assertEquals("+55 11999887766", result.getContact());
        assertEquals(UserRole.PROFESSIONAL, result.getRole());
    }

    @Test
    @DisplayName("Deve retornar null para UserDetails que não é instância de User")
    void deveRetornarNullParaUserDetailsQueNaoEInstanciaDeUser() {
        // Given
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "test@email.com", 
                "password", 
                java.util.Collections.emptyList()
        );

        // When
        UserResponseDto result = Utils.convertToUserResponseDto(userDetails);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Deve retornar null para UserDetails nulo")
    void deveRetornarNullParaUserDetailsNulo() {
        // When
        UserResponseDto result = Utils.convertToUserResponseDto(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Deve converter User com role ADMIN corretamente")
    void deveConverterUserComRoleAdminCorretamente() {
        // Given
        User user = new User();
        user.setId("admin-123");
        user.setName("Admin User");
        user.setEmail("admin@clinic.com");
        user.setContact("+55 11999887766");
        user.setRole(UserRole.ADMIN);

        // When
        UserResponseDto result = Utils.convertToUserResponseDto(user);

        // Then
        assertNotNull(result);
        assertEquals(UserRole.ADMIN, result.getRole());
        assertEquals("Admin User", result.getName());
    }
}