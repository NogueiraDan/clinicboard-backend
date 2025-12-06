package com.clinicboard.notification_service.audit.application.port.in;

import com.clinicboard.notification_service.audit.domain.event.AppointmentAuditEvent;

/**
 * Porta de entrada para processamento de eventos de auditoria.
 * 
 * Define o contrato que a camada de aplicação expõe para
 * adaptadores de entrada (ex: Kafka Consumer).
 * 
 * Segue o padrão Hexagonal Architecture onde portas definem
 * interfaces que os adaptadores implementam ou chamam.
 */
public interface ProcessAuditEventUseCase {
    
    /**
     * Processa um evento de auditoria recebido do Kafka.
     * 
     * @param event Evento de auditoria contendo dados do agendamento
     */
    void processEvent(AppointmentAuditEvent event);
}