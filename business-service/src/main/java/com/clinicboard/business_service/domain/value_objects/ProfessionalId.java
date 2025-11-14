package com.clinicboard.business_service.domain.value_objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * Value Object que representa o ID de um profissional.
 */
@Embeddable
@AttributeOverride(name = "value", column = @Column(name = "professional_id"))
public record ProfessionalId(@Column(name = "professional_id") String value) {
    
    public ProfessionalId {
        Objects.requireNonNull(value, "Professional ID cannot be null");
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Professional ID cannot be empty");
        }
    }
    
    public static ProfessionalId of(String value) {
        return new ProfessionalId(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}