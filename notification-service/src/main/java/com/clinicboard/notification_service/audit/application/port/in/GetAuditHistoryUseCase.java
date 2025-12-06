package com.clinicboard.notification_service.audit.application.port.in;

import com.clinicboard.notification_service.audit.application.dto.AuditLogResponseDto;

import java.util.List;

/**
 * Porta de entrada para consulta de histórico de auditoria.
 */
public interface GetAuditHistoryUseCase {
    
    /**
     * Busca histórico completo de um agendamento.
     */
    List<AuditLogResponseDto> getAppointmentHistory(String appointmentId);
    
    /**
     * Busca histórico de um profissional.
     */
    List<AuditLogResponseDto> getProfessionalHistory(String professionalId);
    
    /**
     * Busca todos os logs de auditoria.
     */
    List<AuditLogResponseDto> getAllAuditLogs();
}