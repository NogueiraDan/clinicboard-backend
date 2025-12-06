package com.clinicboard.business_service.domain.event;

import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento de domínio para auditoria de agendamentos.
 * 
 * Representa um fato imutável que aconteceu no agregado Appointment.
 * Segue o padrão Event Sourcing simplificado para rastreabilidade.
 * 
 * Este evento é publicado no Kafka para consumo pelo módulo de auditoria.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentAuditEvent implements DomainEvent {
    
    @JsonProperty("eventId")
    private String eventId;          // UUID único do evento
    
    @JsonProperty("aggregateId")
    private String aggregateId;      // ID do agendamento
    
    @JsonProperty("eventType")
    private String eventType;        // CREATED, RESCHEDULED, CANCELLED, COMPLETED
    
    @JsonProperty("professionalId")
    private String professionalId;
    
    @JsonProperty("patientId")
    private String patientId;
    
    @JsonProperty("date")
    private String date;
    
    @JsonProperty("hour")
    private String hour;
    
    @JsonProperty("occurredAt")
    private Instant occurredAt;
    
    @JsonProperty("changedBy")
    private String changedBy;        // ID do usuário que fez a mudança
    
    @JsonProperty("metadata")
    private String metadata;         // JSON com dados adicionais
    
    @Override
    public Instant occurredOn() {
        return this.occurredAt;
    }
    
    @Override
    public String getAggregateId() {
        return this.aggregateId;
    }
    
    /**
     * Factory method para criação de eventos de agendamento criado.
     */
    public static AppointmentAuditEvent created(
            String appointmentId,
            String professionalId,
            String patientId,
            String date,
            String hour,
            String changedBy) {
        
        return new AppointmentAuditEvent(
            java.util.UUID.randomUUID().toString(),
            appointmentId,
            "CREATED",
            professionalId,
            patientId,
            date,
            hour,
            Instant.now(),
            changedBy,
            String.format("{\"action\":\"Agendamento criado\",\"date\":\"%s\",\"hour\":\"%s\"}", date, hour)
        );
    }
    
    /**
     * Factory method para criação de eventos de remarcação.
     */
    public static AppointmentAuditEvent rescheduled(
            String appointmentId,
            String professionalId,
            String patientId,
            String oldDate,
            String oldHour,
            String newDate,
            String newHour,
            String changedBy) {
        
        return new AppointmentAuditEvent(
            java.util.UUID.randomUUID().toString(),
            appointmentId,
            "RESCHEDULED",
            professionalId,
            patientId,
            newDate,
            newHour,
            Instant.now(),
            changedBy,
            String.format("{\"action\":\"Agendamento remarcado\",\"from\":{\"date\":\"%s\",\"hour\":\"%s\"},\"to\":{\"date\":\"%s\",\"hour\":\"%s\"}}", 
                oldDate, oldHour, newDate, newHour)
        );
    }
    
    /**
     * Factory method para criação de eventos de cancelamento.
     */
    public static AppointmentAuditEvent cancelled(
            String appointmentId,
            String professionalId,
            String patientId,
            String date,
            String hour,
            String changedBy,
            String reason) {
        
        return new AppointmentAuditEvent(
            java.util.UUID.randomUUID().toString(),
            appointmentId,
            "CANCELLED",
            professionalId,
            patientId,
            date,
            hour,
            Instant.now(),
            changedBy,
            String.format("{\"action\":\"Agendamento cancelado\",\"reason\":\"%s\"}", reason)
        );
    }
}