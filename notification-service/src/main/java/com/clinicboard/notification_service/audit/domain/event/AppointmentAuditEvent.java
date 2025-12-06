package com.clinicboard.notification_service.audit.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Evento de domínio para auditoria de agendamentos.
 * 
 * Esta classe atua como Anti-Corruption Layer (ACL) convertendo
 * eventos externos do Kafka em objetos de domínio internos.
 * 
 * Segue o padrão DDD onde eventos de domínio representam
 * fatos imutáveis que aconteceram no sistema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentAuditEvent {
    
    @JsonProperty("eventId")
    private String eventId;
    
    @JsonProperty("aggregateId")
    private String aggregateId;
    
    @JsonProperty("eventType")
    private String eventType;
    
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
    private String changedBy;
    
    @JsonProperty("metadata")
    private String metadata;
}