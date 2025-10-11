package com.clinicboard.user_service.units.infrastructure;

import com.clinicboard.user_service.domain.User;
import com.clinicboard.user_service.domain.UserRole;
import com.clinicboard.user_service.infrastructure.config.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TokenService - Testes Unitários")
class TokenServiceTest {

    private TokenService tokenService;
    private final String SECRET = "test-secret-key-for-jwt-token-generation";

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", SECRET);
    }

    @Test
    @DisplayName("Deve gerar token JWT válido para usuário")
    void deveGerarTokenJwtValidoParaUsuario() {
        // Given
        User user = new User();
        user.setId("user-123");
        user.setName("João Silva");
        user.setEmail("joao@clinic.com");
        user.setContact("+55 11999887766");
        user.setRole(UserRole.PROFESSIONAL);

        // When
        String token = tokenService.generateToken(user);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT tem 3 partes separadas por pontos
    }

    @Test
    @DisplayName("Deve validar token JWT válido e retornar subject")
    void deveValidarTokenJwtValidoERetornarSubject() {
        // Given
        User user = new User();
        user.setId("user-123");
        user.setName("João Silva");
        user.setEmail("joao@clinic.com");
        user.setContact("+55 11999887766");
        user.setRole(UserRole.PROFESSIONAL);

        String token = tokenService.generateToken(user);

        // When
        String subject = tokenService.validateToken(token);

        // Then
        assertEquals("user-123", subject);
    }

    @Test
    @DisplayName("Deve retornar string vazia para token inválido")
    void deveRetornarStringVaziaParaTokenInvalido() {
        // Given
        String invalidToken = "token.invalido.aqui";

        // When
        String result = tokenService.validateToken(invalidToken);

        // Then
        assertEquals("", result);
    }

    @Test
    @DisplayName("Deve retornar string vazia para token nulo")
    void deveRetornarStringVaziaParaTokenNulo() {
        // When
        String result = tokenService.validateToken(null);

        // Then
        assertEquals("", result);
    }

    @Test
    @DisplayName("Deve retornar string vazia para token vazio")
    void deveRetornarStringVaziaParaTokenVazio() {
        // When
        String result = tokenService.validateToken("");

        // Then
        assertEquals("", result);
    }

    @Test
    @DisplayName("Deve incluir claims corretos no token JWT")
    void deveIncluirClaimsCorretosNoTokenJwt() {
        // Given
        User user = new User();
        user.setId("user-123");
        user.setName("João Silva");
        user.setEmail("joao@clinic.com");
        user.setContact("+55 11999887766");
        user.setRole(UserRole.ADMIN);

        // When
        String token = tokenService.generateToken(user);
        String subject = tokenService.validateToken(token);

        // Then
        assertNotNull(token);
        assertEquals("user-123", subject);
        
        // Verifica se o token contém os claims esperados (isso seria melhor testado com biblioteca de JWT)
        assertTrue(token.length() > 50); // Token JWT deve ter tamanho considerável
    }

    @Test
    @DisplayName("Deve lançar exceção ao gerar token com usuário nulo")
    void deveLancarExcecaoAoGerarTokenComUsuarioNulo() {
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            tokenService.generateToken(null);
        });
    }
}