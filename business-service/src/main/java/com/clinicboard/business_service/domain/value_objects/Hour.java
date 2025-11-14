package com.clinicboard.business_service.domain.value_objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * Value Object que representa um horário de agendamento.
 * 
 * Encapsula as regras de negócio relacionadas a horários:
 * - Formato válido (HH:mm)
 * - Faixa de funcionamento (08:00 às 21:00)
 * - Imutabilidade garantida
 */
@Embeddable
@AttributeOverride(name = "value", column = @Column(name = "hour"))
public record Hour(@Column(name = "hour") String value) {
    
    private static final LocalTime OPENING_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLOSING_TIME = LocalTime.of(21, 0);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    public Hour {
        Objects.requireNonNull(value, "Hour cannot be null");
        validateFormat(value);
        validateBusinessHours(value);
    }
    
    /**
     * Factory method para criação segura
     */
    public static Hour of(String hourString) {
        return new Hour(hourString);
    }
    
    /**
     * Factory method para criação a partir de LocalTime
     */
    public static Hour of(LocalTime time) {
        return new Hour(time.format(FORMATTER));
    }
    
    /**
     * Valida o formato HH:mm
     */
    private void validateFormat(String hour) {
        if (!hour.matches("^([01]\\d|2[0-3]):([0-5]\\d)$")) {
            throw new IllegalArgumentException("Invalid hour format. Expected HH:mm, got: " + hour);
        }
    }
    
    /**
     * Valida se está dentro do horário de funcionamento
     */
    private void validateBusinessHours(String hour) {
        try {
            LocalTime time = LocalTime.parse(hour, FORMATTER);
            
            if (time.isBefore(OPENING_TIME) || time.isAfter(CLOSING_TIME)) {
                throw new IllegalArgumentException(
                    String.format("Hour %s is outside business hours (%s - %s)", 
                        hour, OPENING_TIME, CLOSING_TIME));
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid hour format: " + hour);
        }
    }
    
    /**
     * Converte para LocalTime para cálculos
     */
    public LocalTime toLocalTime() {
        return LocalTime.parse(value, FORMATTER);
    }
    
    /**
     * Verifica se está após outro horário
     */
    public boolean isAfter(Hour other) {
        return this.toLocalTime().isAfter(other.toLocalTime());
    }
    
    /**
     * Verifica se está antes de outro horário
     */
    public boolean isBefore(Hour other) {
        return this.toLocalTime().isBefore(other.toLocalTime());
    }
    
    /**
     * Formata para exibição
     */
    public String format() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
}