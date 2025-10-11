package com.clinicboard.user_service.units.application;

import com.clinicboard.user_service.application.dto.UpdateUserRequestDto;
import com.clinicboard.user_service.application.dto.UserRequestDto;
import com.clinicboard.user_service.application.dto.UserResponseDto;
import com.clinicboard.user_service.application.ports.out.UserPersistencePort;
import com.clinicboard.user_service.application.usecases.UserUseCases;
import com.clinicboard.user_service.domain.UserRole;
import com.clinicboard.user_service.infrastructure.adapters.out.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("UserUseCases - Testes Unitários")
class UserUseCasesTest {

    private UserPersistencePort userPersistencePort;
    private UserUseCases userUseCases;

    @BeforeEach
    void setUp() {
        userPersistencePort = Mockito.mock(UserPersistencePort.class);
        userUseCases = new UserUseCases(userPersistencePort);
    }

    @Test
    @DisplayName("Deve criar usuário com dados válidos")
    void deveCriarUsuarioComDadosValidos() {
        // Given
        UserRequestDto request = new UserRequestDto(
                "João Silva",
                "joao@clinic.com",
                "senha123",
                "+55 11999887766",
                UserRole.PROFESSIONAL
        );

        UserResponseDto expectedResponse = new UserResponseDto(
                "user-123",
                "João Silva",
                "joao@clinic.com",
                "+55 11999887766",
                UserRole.PROFESSIONAL
        );

        when(userPersistencePort.create(any(UserRequestDto.class)))
                .thenReturn(expectedResponse);

        // When
        UserResponseDto result = userUseCases.create(request);

        // Then
        assertNotNull(result);
        assertEquals("user-123", result.getId());
        assertEquals("João Silva", result.getName());
        assertEquals("joao@clinic.com", result.getEmail());
        assertEquals(UserRole.PROFESSIONAL, result.getRole());
        verify(userPersistencePort).create(request);
    }

    @Test
    @DisplayName("Deve retornar lista de todos os usuários")
    void deveRetornarListaDeTodosOsUsuarios() {
        // Given
        List<UserResponseDto> expectedUsers = List.of(
                new UserResponseDto("user-1", "João", "joao@clinic.com", "+55 11999887766", UserRole.PROFESSIONAL),
                new UserResponseDto("user-2", "Maria", "maria@clinic.com", "+55 11999887755", UserRole.ADMIN)
        );

        when(userPersistencePort.findAll()).thenReturn(expectedUsers);

        // When
        List<UserResponseDto> result = userUseCases.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("João", result.get(0).getName());
        assertEquals("Maria", result.get(1).getName());
        verify(userPersistencePort).findAll();
    }

    @Test
    @DisplayName("Deve buscar usuário por ID")
    void deveBuscarUsuarioPorId() {
        // Given
        String userId = "user-123";
        UserResponseDto expectedUser = new UserResponseDto(
                userId,
                "João Silva",
                "joao@clinic.com",
                "+55 11999887766",
                UserRole.PROFESSIONAL
        );

        when(userPersistencePort.findById(userId)).thenReturn(expectedUser);

        // When
        UserResponseDto result = userUseCases.findById(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("João Silva", result.getName());
        verify(userPersistencePort).findById(userId);
    }

    @Test
    @DisplayName("Deve buscar usuário por email")
    void deveBuscarUsuarioPorEmail() {
        // Given
        String email = "joao@clinic.com";
        UserResponseDto expectedUser = new UserResponseDto(
                "user-123",
                "João Silva",
                email,
                "+55 11999887766",
                UserRole.PROFESSIONAL
        );

        when(userPersistencePort.findByEmail(email)).thenReturn(expectedUser);

        // When
        UserResponseDto result = userUseCases.findByEmail(email);

        // Then
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals("João Silva", result.getName());
        verify(userPersistencePort).findByEmail(email);
    }

    @Test
    @DisplayName("Deve atualizar usuário existente")
    void deveAtualizarUsuarioExistente() {
        // Given
        String userId = "user-123";
        UpdateUserRequestDto updateRequest = new UpdateUserRequestDto(
                "João Silva Atualizado",
                "joao.novo@clinic.com",
                "+55 11999887777"
        );

        UserResponseDto expectedResponse = new UserResponseDto(
                userId,
                "João Silva Atualizado",
                "joao.novo@clinic.com",
                "+55 11999887777",
                UserRole.PROFESSIONAL
        );

        when(userPersistencePort.update(userId, updateRequest)).thenReturn(expectedResponse);

        // When
        UserResponseDto result = userUseCases.update(userId, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals("João Silva Atualizado", result.getName());
        assertEquals("joao.novo@clinic.com", result.getEmail());
        verify(userPersistencePort).update(userId, updateRequest);
    }

    @Test
    @DisplayName("Deve deletar usuário existente")
    void deveDeletarUsuarioExistente() {
        // Given
        String userId = "user-123";
        doNothing().when(userPersistencePort).delete(userId);

        // When
        assertDoesNotThrow(() -> userUseCases.delete(userId));

        // Then
        verify(userPersistencePort).delete(userId);
    }

    @Test
    @DisplayName("Deve retornar UserRepository")
    void deveRetornarUserRepository() {
        // Given
        UserRepository mockRepository = Mockito.mock(UserRepository.class);
        when(userPersistencePort.getUserRepository()).thenReturn(mockRepository);

        // When
        UserRepository result = userUseCases.getUserRepository();

        // Then
        assertEquals(mockRepository, result);
        verify(userPersistencePort).getUserRepository();
    }
}