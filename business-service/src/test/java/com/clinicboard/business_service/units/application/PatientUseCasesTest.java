package com.clinicboard.business_service.units.application;

import com.clinicboard.business_service.application.dto.PatientRequestDto;
import com.clinicboard.business_service.application.dto.PatientResponseDto;
import com.clinicboard.business_service.application.port.out.PatientPersistencePort;
import com.clinicboard.business_service.application.usecase.PatientUseCases;
import com.clinicboard.business_service.infrastructure.adapter.in.web.exception.BusinessException;
import com.clinicboard.business_service.infrastructure.adapter.in.web.exception.CustomGenericException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("PatientUseCases - Testes Unitários")
class PatientUseCasesTest {

    private PatientPersistencePort patientPersistencePort;
    private PatientUseCases patientUseCases;

    @BeforeEach
    void setUp() {
        patientPersistencePort = Mockito.mock(PatientPersistencePort.class);
        patientUseCases = new PatientUseCases(patientPersistencePort);
    }

    @Test
    @DisplayName("Deve criar paciente com dados válidos")
    void deveCriarPacienteComDadosValidos() {
        // Given
        PatientRequestDto request = new PatientRequestDto(
                "João Silva",
                30,
                "joao@email.com",
                "+55 11999887766",
                "Histórico médico relevante",
                "user-123"
        );

        PatientResponseDto expectedResponse = new PatientResponseDto(
                "patient-123",
                "João Silva",
                30,
                "joao@email.com",
                "+55 11999887766",
                "Histórico médico relevante",
                "user-123"
        );

        when(patientPersistencePort.create(any(PatientRequestDto.class)))
                .thenReturn(expectedResponse);

        // When
        PatientResponseDto result = patientUseCases.create(request);

        // Then
        assertNotNull(result);
        assertEquals("patient-123", result.getId());
        assertEquals("João Silva", result.getName());
        assertEquals("joao@email.com", result.getEmail());
        verify(patientPersistencePort).create(request);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar paciente com email duplicado")
    void deveLancarExcecaoAoTentarCriarPacienteComEmailDuplicado() {
        // Given
        PatientRequestDto request = new PatientRequestDto(
                "João Silva",
                30,
                "joao@email.com",
                "+55 11999887766",
                "Histórico médico relevante",
                "user-123"
        );

        when(patientPersistencePort.create(any(PatientRequestDto.class)))
                .thenThrow(new BusinessException("Este paciente já está cadastrado no sistema!"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            patientUseCases.create(request);
        });

        assertEquals("Este paciente já está cadastrado no sistema!", exception.getMessage());
        verify(patientPersistencePort).create(request);
    }

    @Test
    @DisplayName("Deve retornar lista de pacientes por usuário")
    void deveRetornarListaDePacientesPorUsuario() {
        // Given
        String userId = "user-123";
        List<PatientResponseDto> expectedPatients = List.of(
                new PatientResponseDto("patient-1", "João", 30, "joao@email.com", "+55 11999887766", null, userId),
                new PatientResponseDto("patient-2", "Maria", 25, "maria@email.com", "+55 11999887755", null, userId)
        );

        when(patientPersistencePort.findByUserId(userId)).thenReturn(expectedPatients);

        // When
        List<PatientResponseDto> result = patientUseCases.findByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("João", result.get(0).getName());
        assertEquals("Maria", result.get(1).getName());
        verify(patientPersistencePort).findByUserId(userId);
    }

    @Test
    @DisplayName("Deve buscar paciente por nome parcial")
    void deveBuscarPacientePorNomeParcial() {
        // Given
        String searchTerm = "João";
        List<PatientResponseDto> expectedPatients = List.of(
                new PatientResponseDto("patient-1", "João Silva", 30, "joao@email.com", "+55 11999887766", null, "user-123"),
                new PatientResponseDto("patient-2", "João Santos", 45, "joaosantos@email.com", "+55 11999887755", null, "user-456")
        );

        when(patientPersistencePort.findPatientByName(searchTerm)).thenReturn(expectedPatients);

        // When
        List<PatientResponseDto> result = patientUseCases.findPatientByName(searchTerm);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.getName().contains("João")));
        verify(patientPersistencePort).findPatientByName(searchTerm);
    }

    @Test
    @DisplayName("Deve atualizar paciente existente")
    void deveAtualizarPacienteExistente() {
        // Given
        String patientId = "patient-123";
        PatientRequestDto updateRequest = new PatientRequestDto(
                "João Silva Atualizado",
                31,
                "joao.novo@email.com",
                "+55 11999887777",
                "Histórico atualizado",
                "user-123"
        );

        PatientResponseDto expectedResponse = new PatientResponseDto(
                patientId,
                "João Silva Atualizado",
                31,
                "joao.novo@email.com",
                "+55 11999887777",
                "Histórico atualizado",
                "user-123"
        );

        when(patientPersistencePort.update(patientId, updateRequest)).thenReturn(expectedResponse);

        // When
        PatientResponseDto result = patientUseCases.update(patientId, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals("João Silva Atualizado", result.getName());
        assertEquals("joao.novo@email.com", result.getEmail());
        verify(patientPersistencePort).update(patientId, updateRequest);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar paciente inexistente")
    void deveLancarExcecaoAoTentarAtualizarPacienteInexistente() {
        // Given
        String patientId = "patient-inexistente";
        PatientRequestDto updateRequest = new PatientRequestDto(
                "João Silva",
                30,
                "joao@email.com",
                "+55 11999887766",
                null,
                "user-123"
        );

        when(patientPersistencePort.update(patientId, updateRequest))
                .thenThrow(new CustomGenericException("Paciente com id " + patientId + " não foi encontrado"));

        // When & Then
        CustomGenericException exception = assertThrows(CustomGenericException.class, () -> {
            patientUseCases.update(patientId, updateRequest);
        });

        assertTrue(exception.getMessage().contains("não foi encontrado"));
        verify(patientPersistencePort).update(patientId, updateRequest);
    }

    @Test
    @DisplayName("Deve deletar paciente existente")
    void deveDeletarPacienteExistente() {
        // Given
        String patientId = "patient-123";
        doNothing().when(patientPersistencePort).delete(patientId);

        // When
        assertDoesNotThrow(() -> patientUseCases.delete(patientId));

        // Then
        verify(patientPersistencePort).delete(patientId);
    }

    @Test
    @DisplayName("Deve buscar paciente por ID")
    void deveBuscarPacientePorId() {
        // Given
        String patientId = "patient-123";
        PatientResponseDto expectedPatient = new PatientResponseDto(
                patientId,
                "João Silva",
                30,
                "joao@email.com",
                "+55 11999887766",
                "Histórico médico",
                "user-123"
        );

        when(patientPersistencePort.findOne(patientId)).thenReturn(expectedPatient);

        // When
        PatientResponseDto result = patientUseCases.findOne(patientId);

        // Then
        assertNotNull(result);
        assertEquals(patientId, result.getId());
        assertEquals("João Silva", result.getName());
        verify(patientPersistencePort).findOne(patientId);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "a", "ab"})
    @DisplayName("Deve retornar lista vazia para busca por nome inválido")
    void deveRetornarListaVaziaParaBuscaPorNomeInvalido(String searchTerm) {
        // Given
        when(patientPersistencePort.findPatientByName(searchTerm)).thenReturn(List.of());

        // When
        List<PatientResponseDto> result = patientUseCases.findPatientByName(searchTerm);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(patientPersistencePort).findPatientByName(searchTerm);
    }
}