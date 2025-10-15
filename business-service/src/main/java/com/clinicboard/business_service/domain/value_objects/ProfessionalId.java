package com.clinicboard.business_service.domain.value_objects;

import java.util.Objects;

import jakarta.persistence.Embeddable;

/**
 * Value Object que representa a identidade de um profissional
 * Encapsula as regras de validação e garante imutabilidade
 */
@Embeddable
public record ProfessionalId(String value) {

    public ProfessionalId {
        Objects.requireNonNull(value, "Professional ID cannot be null");
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Professional ID cannot be empty");
        }
        // Opcional: adicionar validação de formato se necessário
        // if (!value.matches("^[a-fA-F0-9-]{36}$")) {
        // throw new IllegalArgumentException("Professional ID must be a valid UUID");
        // }
    }

    /**
     * Factory method para criar ProfessionalId de forma mais expressiva
     */
    public static ProfessionalId of(String value) {
        return new ProfessionalId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}