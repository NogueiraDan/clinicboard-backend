package com.clinicboard.notification_service.infrastructure.adapter.in.messaging;

import com.clinicboard.notification_service.application.port.in.ProcessAppointmentEventUseCase;
import com.clinicboard.notification_service.domain.event.AppointmentScheduledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada para consumo de eventos via RabbitMQ. * 
 * Implementa Circuit Breaker pattern através de Dead Letter Queue (DLQ)
 * para tratamento de falhas e reprocessamento de mensagens.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQEventConsumer {
    
    private final ProcessAppointmentEventUseCase processAppointmentEventUseCase;
    
    /**
     * Consome eventos de agendamento criado
     */
    @RabbitListener(queues = "${app.messaging.queue.appointment-scheduled}")
    public void handleAppointmentScheduled(AppointmentScheduledEvent event) {
        log.info("Received AppointmentScheduledEvent for appointment: {}", event.getAggregateId());
        
        try {
            processAppointmentEventUseCase.processAppointmentScheduled(event);
            log.debug("Successfully processed AppointmentScheduledEvent for appointment: {}", event.getAggregateId());
        } catch (Exception e) {
            log.error("Failed to process AppointmentScheduledEvent for appointment: {}. Message will be sent to DLQ", 
                event.getAggregateId(), e);
            throw e;
        }
    }
    
    
    /**
     * Consome mensagens da Dead Letter Queue para análise e possível reprocessamento
     */
    @RabbitListener(queues = "${app.messaging.dlq.queue.events-failed}")
    public void handleFailedEvents(Object failedEvent) {
        log.warn("Received failed event in DLQ: {}", failedEvent.getClass().getSimpleName());
        log.warn("Failed event details: {}", failedEvent.toString());
        
        // TODO: Implementar lógica de reprocessamento ou alerta para administradores
        // Possíveis ações:
        // 1. Tentar reprocessar após um tempo
        // 2. Enviar alerta para administradores
        // 3. Armazenar em banco para análise posterior
        // 4. Aplicar estratégias de retry com backoff
    }
}
