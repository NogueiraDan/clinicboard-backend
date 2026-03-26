package com.clinicboard.business_service.units.application;

import com.clinicboard.business_service.application.dto.AppointmentRequestDto;
import com.clinicboard.business_service.application.dto.AppointmentResponseDto;
import com.clinicboard.business_service.application.port.out.AppointmentPersistencePort;
import com.clinicboard.business_service.application.port.out.EventPublisherGateway;
import com.clinicboard.business_service.application.usecase.AppointmentUseCases;
import com.clinicboard.business_service.domain.event.AppointmentScheduledEvent;
import com.clinicboard.business_service.domain.model.AppointmentType;
import com.clinicboard.business_service.domain.service.AppointmentSchedulingService;
import com.clinicboard.business_service.infrastructure.adapter.in.web.exception.BusinessException;
import com.clinicboard.business_service.infrastructure.adapter.out.quartz.AppointmentReminderScheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AppointmentUseCases - Testes Unitários")
class AppointmentUseCasesTest {

    private AppointmentPersistencePort appointmentPersistencePort;
    private EventPublisherGateway eventPublisher;
    private AppointmentSchedulingService appointmentSchedulingService;
    private AppointmentUseCases appointmentUseCases;
    private AppointmentReminderScheduler appointmentReminderScheduler;

    @BeforeEach
    void setUp() {
        appointmentPersistencePort = Mockito.mock(AppointmentPersistencePort.class);
        eventPublisher = Mockito.mock(EventPublisherGateway.class);
        appointmentSchedulingService = Mockito.mock(AppointmentSchedulingService.class);
        appointmentReminderScheduler = Mockito.mock(AppointmentReminderScheduler.class);
        appointmentUseCases = new AppointmentUseCases(appointmentPersistencePort, eventPublisher, appointmentReminderScheduler, appointmentSchedulingService);
    }

    @Test
    @DisplayName("Deve criar agendamento e publicar evento de domínio")
    void deveCriarAgendamentoEPublicarEventoDeDominio() {
        // Given
        AppointmentRequestDto request = new AppointmentRequestDto(
                LocalDate.now().plusDays(1),
                "10:00",
                AppointmentType.MARCACAO,
                "user-123",
                "patient-456"
        );

        AppointmentResponseDto response = new AppointmentResponseDto(
                "appointment-789",
                LocalDate.now().plusDays(1),
                "10:00",
                AppointmentType.MARCACAO,
                "user-123",
                "patient-456"
        );

        when(appointmentPersistencePort.create(request)).thenReturn(response);
        doNothing().when(eventPublisher).publishAppointmentScheduled(any(AppointmentScheduledEvent.class));

        // When
        AppointmentResponseDto result = appointmentUseCases.create(request);

        // Then
        assertNotNull(result);
        assertEquals("appointment-789", result.getId());
        assertEquals(AppointmentType.MARCACAO, result.getType());

        // Verifica se evento foi publicado com dados corretos
        ArgumentCaptor<AppointmentScheduledEvent> eventCaptor = ArgumentCaptor.forClass(AppointmentScheduledEvent.class);
        verify(eventPublisher).publishAppointmentScheduled(eventCaptor.capture());
        
        AppointmentScheduledEvent publishedEvent = eventCaptor.getValue();
        assertEquals("appointment-789", publishedEvent.getAggregateId());
        assertEquals("patient-456", publishedEvent.getPatientIdValue());
        assertEquals("user-123", publishedEvent.getProfessionalIdValue());
    }

    @Test
    @DisplayName("Deve lançar exceção quando falhar ao publicar evento")
    void deveLancarExcecaoQuandoFalharAoPublicarEvento() {
        // Given
        AppointmentRequestDto request = new AppointmentRequestDto(
                LocalDate.now().plusDays(1),
                "10:00",
                AppointmentType.MARCACAO,
                "user-123",
                "patient-456"
        );

        AppointmentResponseDto response = new AppointmentResponseDto(
                "appointment-789",
                LocalDate.now().plusDays(1),
                "10:00",
                AppointmentType.MARCACAO,
                "user-123",
                "patient-456"
        );

        when(appointmentPersistencePort.create(request)).thenReturn(response);
        doThrow(new RuntimeException("Falha na publicação do evento"))
                .when(eventPublisher).publishAppointmentScheduled(any());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            appointmentUseCases.create(request);
        });

        assertTrue(exception.getMessage().contains("Falha ao publicar evento de agendamento"));
        verify(appointmentPersistencePort).create(request);
        verify(eventPublisher).publishAppointmentScheduled(any());
    }

    @ParameterizedTest
    @EnumSource(AppointmentType.class)
    @DisplayName("Deve permitir criação com todos os tipos de agendamento")
    void devePermitirCriacaoComTodosTiposDeAgendamento(AppointmentType type) {
        // Given
        AppointmentRequestDto request = new AppointmentRequestDto(
                LocalDate.now().plusDays(1),
                "14:00",
                type,
                "user-123",
                "patient-456"
        );

        AppointmentResponseDto response = new AppointmentResponseDto(
                "appointment-" + type,
                LocalDate.now().plusDays(1),
                "14:00",
                type,
                "user-123",
                "patient-456"
        );

        when(appointmentPersistencePort.create(any())).thenReturn(response);

        // When
        AppointmentResponseDto result = appointmentUseCases.create(request);

        // Then
        assertNotNull(result);
        assertEquals(type, result.getType());
        verify(appointmentPersistencePort).create(request);
        verify(eventPublisher).publishAppointmentScheduled(any());
    }

    @Test
    @DisplayName("Deve buscar horários disponíveis para uma data específica")
    void deveBuscarHorariosDisponiveisParaDataEspecifica() {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        String userId = "user-123";
        List<String> availableHours = List.of("08:00", "09:00", "10:00", "14:00", "15:00");

        when(appointmentPersistencePort.findAvailableHours(date, userId)).thenReturn(availableHours);

        // When
        List<String> result = appointmentUseCases.findAvailableHours(date, userId);

        // Then
        assertNotNull(result);
        assertEquals(5, result.size());
        assertTrue(result.contains("08:00"));
        assertTrue(result.contains("15:00"));
        verify(appointmentPersistencePort).findAvailableHours(date, userId);
    }

    @Test
    @DisplayName("Deve buscar agendamentos por usuário e data")
    void deveBuscarAgendamentosPorUsuarioEData() {
        // Given
        String userId = "user-123";
        LocalDate date = LocalDate.now().plusDays(1);
        List<AppointmentResponseDto> expectedAppointments = List.of(
                new AppointmentResponseDto("appt-1", date, "10:00", AppointmentType.MARCACAO, userId, "patient-1"),
                new AppointmentResponseDto("appt-2", date, "14:00", AppointmentType.REMARCACAO, userId, "patient-2")
        );

        when(appointmentPersistencePort.findAppointments(userId, date)).thenReturn(expectedAppointments);

        // When
        List<AppointmentResponseDto> result = appointmentUseCases.findAppointments(userId, date);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("10:00", result.get(0).getHour());
        assertEquals("14:00", result.get(1).getHour());
        verify(appointmentPersistencePort).findAppointments(userId, date);
    }

    @Test
    @DisplayName("Deve atualizar agendamento apenas com tipo REMARCACAO")
    void deveAtualizarAgendamentoApenasComTipoRemarcacao() {
        // Given
        String appointmentId = "appointment-123";
        AppointmentRequestDto updateRequest = new AppointmentRequestDto(
                LocalDate.now().plusDays(2),
                "16:00",
                AppointmentType.REMARCACAO,
                "user-123",
                "patient-456"
        );

        AppointmentResponseDto updatedResponse = new AppointmentResponseDto(
                appointmentId,
                LocalDate.now().plusDays(2),
                "16:00",
                AppointmentType.REMARCACAO,
                "user-123",
                "patient-456"
        );

        when(appointmentPersistencePort.update(appointmentId, updateRequest)).thenReturn(updatedResponse);

        // When
        AppointmentResponseDto result = appointmentUseCases.update(appointmentId, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(AppointmentType.REMARCACAO, result.getType());
        assertEquals("16:00", result.getHour());
        verify(appointmentPersistencePort).update(appointmentId, updateRequest);
        // Não deve publicar evento em atualização
        verify(eventPublisher, never()).publishAppointmentScheduled(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar com tipo MARCACAO")
    void deveLancarExcecaoAoTentarAtualizarComTipoMarcacao() {
        // Given
        String appointmentId = "appointment-123";
        AppointmentRequestDto updateRequest = new AppointmentRequestDto(
                LocalDate.now().plusDays(2),
                "16:00",
                AppointmentType.MARCACAO, // Tipo inválido para atualização
                "user-123",
                "patient-456"
        );

        when(appointmentPersistencePort.update(appointmentId, updateRequest))
                .thenThrow(new BusinessException("Tipo de agendamento inválido para atualização"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appointmentUseCases.update(appointmentId, updateRequest);
        });

        assertTrue(exception.getMessage().contains("inválido para atualização"));
        verify(appointmentPersistencePort).update(appointmentId, updateRequest);
    }

    @Test
    @DisplayName("Deve deletar agendamento existente")
    void deveDeletarAgendamentoExistente() {
        // Given
        String appointmentId = "appointment-123";
        doNothing().when(appointmentPersistencePort).delete(appointmentId);

        // When
        assertDoesNotThrow(() -> appointmentUseCases.delete(appointmentId));

        // Then
        verify(appointmentPersistencePort).delete(appointmentId);
        // Não deve publicar evento em deleção
        verify(eventPublisher, never()).publishAppointmentScheduled(any());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há horários disponíveis")
    void deveRetornarListaVaziaQuandoNaoHaHorariosDisponiveis() {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        String userId = "user-123";

        when(appointmentPersistencePort.findAvailableHours(date, userId)).thenReturn(List.of());

        // When
        List<String> result = appointmentUseCases.findAvailableHours(date, userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(appointmentPersistencePort).findAvailableHours(date, userId);
    }
}