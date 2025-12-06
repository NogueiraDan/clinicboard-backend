package com.clinicboard.notification_service.audit.application.port.out;

import com.clinicboard.notification_service.audit.domain.model.AuditLog;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saída para persistência de logs de auditoria.
 * 
 * Define o contrato que a camada de aplicação espera que
 * a infraestrutura implemente para acesso a dados.
 * 
 * Segue o padrão Dependency Inversion Principle onde
 * o domínio/aplicação define a interface e a infraestrutura implementa.
 */
public interface AuditLogPersistencePort {
    
    /**
     * Salva um registro de auditoria no banco de dados.
     * 
     * @param auditLog Log de auditoria a ser persistido
     * @return Log de auditoria salvo
     */
    AuditLog save(AuditLog auditLog);
    
    /**
     * Busca todos os logs de auditoria de um agendamento específico.
     * 
     * Permite reconstruir o histórico completo de mudanças.
     * 
     * @param aggregateId ID do agendamento
     * @return Lista de eventos ordenados cronologicamente
     */
    List<AuditLog> findByAggregateId(String aggregateId);
    
    /**
     * Busca um log de auditoria por ID do evento.
     * 
     * @param eventId ID único do evento
     * @return Optional contendo o log se encontrado
     */
    Optional<AuditLog> findByEventId(String eventId);
    
    /**
     * Busca todos os logs de auditoria de um profissional.
     * 
     * @param professionalId ID do profissional
     * @return Lista de eventos relacionados ao profissional
     */
    List<AuditLog> findByProfessionalId(String professionalId);

    /**
     * Busca todos os logs de auditoria.
     * 
     * @return Lista completa de eventos ordenados por data
     */
    List<AuditLog> findAll();
}