package com.clinicboard.notification_service.audit.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO de resposta para logs de auditoria.
 * 
 * Representa a visualização externa dos eventos de auditoria
 * sem expor a estrutura interna do domínio.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponseDto {
    
    private String eventId;
    private String aggregateId;
    private String eventType;
    private String professionalId;
    private String patientId;
    private String date;
    private String hour;
    private Instant occurredAt;
    private String changedBy;
    private String metadata;
}