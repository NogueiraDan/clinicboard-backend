package com.clinicboard.business_service.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Evento de domínio disparado quando um novo agendamento é criado.
 */
public record AppointmentScheduledEvent(
        @JsonProperty("aggregateId") String appointmentId,
        @JsonProperty("patientId") String patientId,
        @JsonProperty("professionalId") String professionalId,
        @JsonProperty("date") String date,
        @JsonProperty("hour") String hour,
        @JsonProperty("message") String message,
        @JsonProperty("createdAt") Instant createdAt) implements DomainEvent {

    /**
     * Factory method para criar o evento a partir de um agendamento
     */
    public static AppointmentScheduledEvent from(
            String appointmentId,
            String patientId,
            String professionalId,
            String date,
            String hour) {
        
        // Gera mensagem personalizada
        String formattedMessage = generateMessage(date, hour);
        
        return new AppointmentScheduledEvent(
                appointmentId,
                patientId,
                professionalId,
                date,
                hour,
                formattedMessage,
                Instant.now());
    }

    private static String generateMessage(String date, String hour) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            String formattedDate = localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            
            return String.format("Seu agendamento foi marcado para %s às %s", 
                    formattedDate, hour);
        } catch (Exception e) {
            // Fallback caso haja erro na formatação
            return String.format("Seu agendamento foi marcado para %s às %s", date, hour);
        }
    }

    @Override
    public String getAggregateId() {
        return appointmentId;
    }

    @Override
    public Instant occurredOn() {
        return createdAt;
    }

    // Getters adicionais para facilitar o uso
    public String getPatientIdValue() {
        return patientId;
    }

    public String getProfessionalIdValue() {
        return professionalId;
    }

    public String getDateValue() {
        return date;
    }

    public String getMessageValue() {
        return message;
    }
}