package com.clinicboard.user_service.units.infrastructure;

import com.clinicboard.user_service.application.dto.UpdateUserRequestDto;
import com.clinicboard.user_service.application.dto.UserRequestDto;
import com.clinicboard.user_service.application.dto.UserResponseDto;
import com.clinicboard.user_service.domain.User;
import com.clinicboard.user_service.domain.UserRole;
import com.clinicboard.user_service.infrastructure.adapters.out.persistence.UserMapper;
import com.clinicboard.user_service.infrastructure.adapters.out.persistence.UserPersistencePortImpl;
import com.clinicboard.user_service.infrastructure.adapters.out.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("UserPersistencePortImpl - Testes Unitários")
class UserPersistencePortImplTest {

    private UserRepository userRepository;
    private UserMapper userMapper;
    private UserPersistencePortImpl userPersistencePort;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        userMapper = Mockito.mock(UserMapper.class);
        userPersistencePort = new UserPersistencePortImpl(userRepository, userMapper);
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

        User userEntity = new User();
        userEntity.setName("João Silva");
        userEntity.setEmail("joao@clinic.com");

        User savedUser = new User();
        savedUser.setId("user-123");
        savedUser.setName("João Silva");
        savedUser.setEmail("joao@clinic.com");

        UserResponseDto expectedResponse = new UserResponseDto(
                "user-123",
                "João Silva",
                "joao@clinic.com",
                "+55 11999887766",
                UserRole.PROFESSIONAL
        );

        when(userMapper.toEntity(request)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(expectedResponse);

        // When
        UserResponseDto result = userPersistencePort.create(request);

        // Then
        assertNotNull(result);
        assertEquals("user-123", result.getId());
        assertEquals("João Silva", result.getName());
        verify(userMapper).toEntity(request);
        verify(userRepository).save(userEntity);
        verify(userMapper).toDto(savedUser);
    }

    @Test
    @DisplayName("Deve retornar todos os usuários")
    void deveRetornarTodosOsUsuarios() {
        // Given
        List<User> users = List.of(
                createUser("user-1", "João", "joao@clinic.com"),
                createUser("user-2", "Maria", "maria@clinic.com")
        );

        List<UserResponseDto> expectedDtos = List.of(
                new UserResponseDto("user-1", "João", "joao@clinic.com", "+55 11999887766", UserRole.PROFESSIONAL),
                new UserResponseDto("user-2", "Maria", "maria@clinic.com", "+55 11999887755", UserRole.ADMIN)
        );

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDto(users.get(0))).thenReturn(expectedDtos.get(0));
        when(userMapper.toDto(users.get(1))).thenReturn(expectedDtos.get(1));

        // When
        List<UserResponseDto> result = userPersistencePort.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("João", result.get(0).getName());
        assertEquals("Maria", result.get(1).getName());
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Deve buscar usuário por ID")
    void deveBuscarUsuarioPorId() {
        // Given
        String userId = "user-123";
        User user = createUser(userId, "João Silva", "joao@clinic.com");
        UserResponseDto expectedDto = new UserResponseDto(
                userId, "João Silva", "joao@clinic.com", "+55 11999887766", UserRole.PROFESSIONAL
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(expectedDto);

        // When
        UserResponseDto result = userPersistencePort.findById(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("João Silva", result.getName());
        verify(userRepository).findById(userId);
        verify(userMapper).toDto(user);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar usuário inexistente por ID")
    void deveLancarExcecaoAoBuscarUsuarioInexistentePorId() {
        // Given
        String userId = "user-inexistente";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userPersistencePort.findById(userId);
        });

        assertTrue(exception.getMessage().contains("não foi encontrado"));
        verify(userRepository).findById(userId);
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

        User existingUser = createUser(userId, "João Silva", "joao@clinic.com");
        User updatedUser = createUser(userId, "João Silva Atualizado", "joao.novo@clinic.com");
        
        UserResponseDto expectedResponse = new UserResponseDto(
                userId,
                "João Silva Atualizado",
                "joao.novo@clinic.com",
                "+55 11999887777",
                UserRole.PROFESSIONAL
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(expectedResponse);

        // When
        UserResponseDto result = userPersistencePort.update(userId, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals("João Silva Atualizado", result.getName());
        assertEquals("joao.novo@clinic.com", result.getEmail());
        verify(userRepository).findById(userId);
        verify(userMapper).updateUserFromDto(updateRequest, existingUser);
        verify(userRepository).save(existingUser);
        verify(userMapper).toDto(updatedUser);
    }

    @Test
    @DisplayName("Deve deletar usuário por ID")
    void deveDeletarUsuarioPorId() {
        // Given
        String userId = "user-123";
        doNothing().when(userRepository).deleteById(userId);

        // When
        assertDoesNotThrow(() -> userPersistencePort.delete(userId));

        // Then
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("Deve retornar UserRepository")
    void deveRetornarUserRepository() {
        // When
        UserRepository result = userPersistencePort.getUserRepository();

        // Then
        assertEquals(userRepository, result);
    }

    private User createUser(String id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setContact("+55 11999887766");
        user.setRole(UserRole.PROFESSIONAL);
        return user;
    }
}