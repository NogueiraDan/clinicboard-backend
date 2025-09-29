package com.clinicboard.business_service.domain.event;

import java.time.Instant;

/**
 * Interface base para todos os eventos de domínio.
 * 
 * Representa algo significativo que aconteceu no domínio e que outros
 * bounded contexts ou componentes podem precisar saber.
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
