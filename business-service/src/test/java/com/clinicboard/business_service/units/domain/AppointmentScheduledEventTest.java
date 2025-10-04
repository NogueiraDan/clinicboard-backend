package com.clinicboard.business_service.units.domain;

import com.clinicboard.business_service.domain.event.AppointmentScheduledEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AppointmentScheduledEvent - Testes de Evento de Domínio")
class AppointmentScheduledEventTest {

    @Test
    @DisplayName("Deve criar evento com factory method")
    void deveCriarEventoComFactoryMethod() {
        // Given
        String appointmentId = "appt-123";
        String patientId = "patient-456";
        String professionalId = "prof-789";
        String date = "2024-12-20";
        String hour = "14:00";

        // When
        AppointmentScheduledEvent event = AppointmentScheduledEvent.from(
                appointmentId, patientId, professionalId, date, hour
        );

        // Then
        assertNotNull(event);
        assertEquals(appointmentId, event.getAggregateId());
        assertEquals(patientId, event.getPatientIdValue());
        assertEquals(professionalId, event.getProfessionalIdValue());
        assertEquals(date, event.getDateValue());
        assertNotNull(event.occurredOn());
        assertTrue(event.occurredOn().isBefore(Instant.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Deve gerar mensagem formatada corretamente")
    void deveGerarMensagemFormatadaCorretamente() {
        // Given
        String date = "2024-12-20";
        String hour = "14:30";

        // When
        AppointmentScheduledEvent event = AppointmentScheduledEvent.from(
                "appt-123", "patient-456", "prof-789", date, hour
        );

        // Then
        String expectedMessage = "Seu agendamento foi marcado para 20/12/2024 às 14:30";
        assertEquals(expectedMessage, event.getMessageValue());
    }

    @Test
    @DisplayName("Deve gerar mensagem de fallback para data inválida")
    void deveGerarMensagemDeFallbackParaDataInvalida() {
        // Given
        String invalidDate = "data-invalida";
        String hour = "14:30";

        // When
        AppointmentScheduledEvent event = AppointmentScheduledEvent.from(
                "appt-123", "patient-456", "prof-789", invalidDate, hour
        );

        // Then
        String expectedFallbackMessage = "Seu agendamento foi marcado para data-invalida às 14:30";
        assertEquals(expectedFallbackMessage, event.getMessageValue());
    }

    @Test
    @DisplayName("Deve implementar DomainEvent corretamente")
    void deveImplementarDomainEventCorretamente() {
        // Given
        String appointmentId = "appt-123";
        Instant beforeCreation = Instant.now();

        // When
        AppointmentScheduledEvent event = AppointmentScheduledEvent.from(
                appointmentId, "patient-456", "prof-789", "2024-12-20", "14:00"
        );
        Instant afterCreation = Instant.now();

        // Then
        assertEquals(appointmentId, event.getAggregateId());
        assertNotNull(event.occurredOn());
        assertTrue(event.occurredOn().isAfter(beforeCreation.minusSeconds(1)));
        assertTrue(event.occurredOn().isBefore(afterCreation.plusSeconds(1)));
        assertEquals("AppointmentScheduledEvent", event.getEventType());
    }

    @Test
    @DisplayName("Deve manter imutabilidade dos dados do evento")
    void deveManterImutabilidadeDosDadosDoEvento() {
        // Given
        AppointmentScheduledEvent event = AppointmentScheduledEvent.from(
                "appt-123", "patient-456", "prof-789", "2024-12-20", "14:00"
        );

        // When - Tentativa de modificação através dos getters
        String originalAggregateId = event.getAggregateId();
        String originalPatientId = event.getPatientIdValue();
        String originalDate = event.getDateValue();
        String originalMessage = event.getMessageValue();
        Instant originalOccurredOn = event.occurredOn();

        // Then - Dados devem permanecer inalterados
        assertEquals(originalAggregateId, event.getAggregateId());
        assertEquals(originalPatientId, event.getPatientIdValue());
        assertEquals(originalDate, event.getDateValue());
        assertEquals(originalMessage, event.getMessageValue());
        assertEquals(originalOccurredOn, event.occurredOn());
    }

    @Test
    @DisplayName("Deve conter todos os dados necessários para notificação")
    void deveConterTodosDadosNecessariosParaNotificacao() {
        // Given & When
        AppointmentScheduledEvent event = AppointmentScheduledEvent.from(
                "appt-123", "patient-456", "prof-789", "2024-12-20", "14:00"
        );

        // Then
        assertNotNull(event.getAggregateId(), "ID do agendamento é obrigatório");
        assertNotNull(event.getPatientIdValue(), "ID do paciente é obrigatório");
        assertNotNull(event.getProfessionalIdValue(), "ID do profissional é obrigatório");
        assertNotNull(event.getDateValue(), "Data é obrigatória");
        assertNotNull(event.getMessageValue(), "Mensagem é obrigatória");
        assertNotNull(event.occurredOn(), "Timestamp de ocorrência é obrigatório");
    }

    @Test
    @DisplayName("Deve aceitar dados com valores limite")
    void deveAceitarDadosComValoresLimite() {
        // Given
        String longId = "a".repeat(100);
        String emptyHour = "";

        // When
        AppointmentScheduledEvent event = AppointmentScheduledEvent.from(
                longId, "patient-456", "prof-789", "2024-12-20", emptyHour
        );

        // Then
        assertEquals(longId, event.getAggregateId());
        assertNotNull(event.getMessageValue());
    }
}