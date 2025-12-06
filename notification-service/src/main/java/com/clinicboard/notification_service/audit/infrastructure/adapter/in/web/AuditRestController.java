package com.clinicboard.notification_service.audit.infrastructure.adapter.in.web;

import com.clinicboard.notification_service.audit.application.dto.AuditLogResponseDto;
import com.clinicboard.notification_service.audit.application.port.in.GetAuditHistoryUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller para consulta de histórico de auditoria.
 * 
 * Endpoints:
 * - GET /audit/appointment/{appointmentId} - Histórico de um agendamento
 * - GET /audit/professional/{professionalId} - Histórico de um profissional
 * - GET /audit - Todos os logs de auditoria
 * 
 * Segue padrão REST e Hexagonal Architecture.
 */
@Slf4j
@RestController
@RequestMapping("audit")
@RequiredArgsConstructor
public class AuditRestController {
    
    private final GetAuditHistoryUseCase getAuditHistoryUseCase;
    
    /**
     * Busca histórico completo de mudanças de um agendamento.
     * 
     * Permite rastreabilidade completa: criação, remarcações, cancelamentos.
     * 
     * @param appointmentId ID do agendamento
     * @return Lista cronológica de eventos
     */
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<AuditLogResponseDto>> getAppointmentHistory(
            @PathVariable String appointmentId) {
        
        log.info("🔍 GET /audit/appointment/{} - Consultando histórico", appointmentId);
        
        List<AuditLogResponseDto> history = getAuditHistoryUseCase.getAppointmentHistory(appointmentId);
        
        if (history.isEmpty()) {
            log.warn("⚠️ Nenhum evento encontrado para agendamento: {}", appointmentId);
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(history);
    }
    
    /**
     * Busca histórico de todos os eventos de um profissional.
     * 
     * @param professionalId ID do profissional
     * @return Lista de eventos ordenada por data (mais recente primeiro)
     */
    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<List<AuditLogResponseDto>> getProfessionalHistory(
            @PathVariable String professionalId) {
        
        log.info("🔍 GET /audit/professional/{} - Consultando histórico", professionalId);
        
        List<AuditLogResponseDto> history = getAuditHistoryUseCase.getProfessionalHistory(professionalId);
        
        if (history.isEmpty()) {
            log.warn("⚠️ Nenhum evento encontrado para profissional: {}", professionalId);
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(history);
    }
    
    /**
     * Busca todos os logs de auditoria do sistema.
     * 
     * ⚠️ Endpoint para admin/compliance - pode ter grande volume de dados.
     * Em produção, considere adicionar paginação.
     * 
     * @return Lista completa de eventos ordenada por data
     */
    @GetMapping
    public ResponseEntity<List<AuditLogResponseDto>> getAllAuditLogs() {
        
        log.info("🔍 GET /audit - Consultando todos os logs");
        
        List<AuditLogResponseDto> allLogs = getAuditHistoryUseCase.getAllAuditLogs();
        
        if (allLogs.isEmpty()) {
            log.warn("⚠️ Nenhum evento de auditoria encontrado");
            return ResponseEntity.noContent().build();
        }
        
        log.info("✅ Retornando {} eventos de auditoria", allLogs.size());
        
        return ResponseEntity.ok(allLogs);
    }
}