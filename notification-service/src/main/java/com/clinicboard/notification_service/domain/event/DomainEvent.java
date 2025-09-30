package com.clinicboard.notification_service.domain.event;

import java.time.Instant;

/**
 * Interface base para todos os eventos de domínio no contexto de notificação.
 * 
 * Representa eventos significativos que aconteceram em outros bounded contexts
 * e que precisam ser processados pelo serviço de notificação.
 */
public interface DomainEvent {
    
    /**
     * Retorna o momento em que o evento ocorreu
     */
    Instant occurredOn();
    
    /**
     * Retorna o tipo do evento para fins de roteamento/processamento
     */
    default String getEventType() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * Retorna o ID da entidade que gerou o evento (se aplicável)
     */
    default String getAggregateId() {
        return null;
    }
}
