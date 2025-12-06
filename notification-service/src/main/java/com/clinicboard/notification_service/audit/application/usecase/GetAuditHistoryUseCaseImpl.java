package com.clinicboard.notification_service.audit.application.usecase;

import com.clinicboard.notification_service.audit.application.dto.AuditLogResponseDto;
import com.clinicboard.notification_service.audit.application.port.in.GetAuditHistoryUseCase;
import com.clinicboard.notification_service.audit.application.port.out.AuditLogPersistencePort;
import com.clinicboard.notification_service.audit.domain.model.AuditLog;
import com.clinicboard.notification_service.audit.infrastructure.adapter.in.web.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementação do caso de uso de consulta de histórico de auditoria.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetAuditHistoryUseCaseImpl implements GetAuditHistoryUseCase {
    
    private final AuditLogPersistencePort auditLogPersistencePort;
    private final AuditLogMapper auditLogMapper;
    
    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getAppointmentHistory(String appointmentId) {
        log.info("📋 Buscando histórico de auditoria para agendamento: {}", appointmentId);
        
        List<AuditLog> auditLogs = auditLogPersistencePort.findByAggregateId(appointmentId);
        
        log.info("✅ Encontrados {} eventos para agendamento {}", auditLogs.size(), appointmentId);
        
        return auditLogMapper.toDtoList(auditLogs);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getProfessionalHistory(String professionalId) {
        log.info("📋 Buscando histórico de auditoria para profissional: {}", professionalId);
        
        List<AuditLog> auditLogs = auditLogPersistencePort.findByProfessionalId(professionalId);
        
        log.info("✅ Encontrados {} eventos para profissional {}", auditLogs.size(), professionalId);
        
        return auditLogMapper.toDtoList(auditLogs);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getAllAuditLogs() {
        log.info("📋 Buscando todos os logs de auditoria");
        
        List<AuditLog> auditLogs = auditLogPersistencePort.findAll();
        
        log.info("✅ Encontrados {} eventos de auditoria", auditLogs.size());
        
        return auditLogMapper.toDtoList(auditLogs);
    }
}