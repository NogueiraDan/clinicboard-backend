package com.clinicboard.notification_service.audit.infrastructure.adapter.in.web;

import com.clinicboard.notification_service.audit.application.dto.AuditLogResponseDto;
import com.clinicboard.notification_service.audit.domain.model.AuditLog;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversão entre entidade de domínio e DTO.
 */
@Component
public class AuditLogMapper {
    
    /**
     * Converte entidade de domínio para DTO de resposta.
     */
    public AuditLogResponseDto toDto(AuditLog auditLog) {
        return new AuditLogResponseDto(
            auditLog.getEventId(),
            auditLog.getAggregateId(),
            auditLog.getEventType(),
            auditLog.getProfessionalId(),
            auditLog.getPatientId(),
            auditLog.getDate(),
            auditLog.getHour(),
            auditLog.getOccurredAt(),
            auditLog.getChangedBy(),
            auditLog.getMetadata()
        );
    }
    
    /**
     * Converte lista de entidades para lista de DTOs.
     */
    public List<AuditLogResponseDto> toDtoList(List<AuditLog> auditLogs) {
        return auditLogs.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
}