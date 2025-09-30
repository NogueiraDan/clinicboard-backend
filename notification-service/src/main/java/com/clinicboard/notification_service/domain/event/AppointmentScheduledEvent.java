package com.clinicboard.notification_service.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Evento de domínio que representa o agendamento de uma consulta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentScheduledEvent implements DomainEvent {
    
    @JsonProperty("aggregateId")
    private String aggregateId;
    
    @JsonProperty("patientId")
    private String patientId;

    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("date")
    private String date;

    @JsonProperty("hour")
    private String hour;

    @JsonProperty("message")
    private String message;

    @JsonProperty("createdAt")
    private Instant createdAt;

    @Override
    public Instant occurredOn() {
        return this.createdAt;
    }
    
    @Override
    public String getAggregateId() {
        return this.aggregateId;
    }
}
