package com.clinicboard.notification_service.audit.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate Root do Bounded Context de Auditoria.
 * 
 * Representa um registro imutável de eventos que ocorreram no sistema,
 * seguindo o padrão Event Sourcing simplificado.
 * 
 * Responsabilidades:
 * - Armazenar fatos históricos de mudanças em agendamentos
 * - Garantir rastreabilidade completa (quem, quando, o quê)
 * - Fornecer base para reconstrução de estado e compliance
 * 
 * Seguindo DDD:
 * - É um Aggregate Root (gerencia seu próprio ciclo de vida)
 * - É imutável após criação (eventos são fatos do passado)
 * - Contém identidade única (eventId)
 */
@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    
    @Id
    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;
    
    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;      // ID do agendamento
    
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;        // CREATED, RESCHEDULED, CANCELLED, COMPLETED
    
    @Column(name = "professional_id")
    private String professionalId;
    
    @Column(name = "patient_id")
    private String patientId;
    
    @Column(name = "appointment_date")
    private String date;
    
    @Column(name = "appointment_hour")
    private String hour;
    
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
    
    @Column(name = "changed_by")
    private String changedBy;        // ID do usuário que fez a mudança
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;         // JSON com dados adicionais
    
    /**
     * Factory method para criação a partir de evento de domínio.
     * 
     * Segue o padrão Domain-Driven Design onde a criação de agregados
     * é controlada por métodos de fábrica que garantem invariantes.
     */
    public static AuditLog fromEvent(
            String eventId,
            String aggregateId,
            String eventType,
            String professionalId,
            String patientId,
            String date,
            String hour,
            Instant occurredAt,
            String changedBy,
            String metadata) {
        
        Objects.requireNonNull(eventId, "Event ID cannot be null");
        Objects.requireNonNull(aggregateId, "Aggregate ID cannot be null");
        Objects.requireNonNull(eventType, "Event type cannot be null");
        Objects.requireNonNull(occurredAt, "Occurred timestamp cannot be null");
        
        return new AuditLog(
            eventId,
            aggregateId,
            eventType,
            professionalId,
            patientId,
            date,
            hour,
            occurredAt,
            changedBy,
            metadata
        );
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLog auditLog = (AuditLog) o;
        return Objects.equals(eventId, auditLog.eventId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }
}